import builtins
import contextlib
import ctypes
import io
import json
import os
import sys
import ast
import tokenize
import traceback
import unittest


_NATIVE_LIBRARY_HANDLES = {}
_MAX_CAPTURE_CHARS = 1_000_000
_CAPTURE_TRIM_CHARS = 100_000
_IGNORED_SOURCE_DIRECTORIES = {
    ".git",
    ".ssh",
    ".mypy_cache",
    ".pytest_cache",
    ".ruff_cache",
    "__pycache__",
}


class _CappedStringIO(io.StringIO):
    def write(self, value):
        written = super().write(value)
        if self.tell() > _MAX_CAPTURE_CHARS + _CAPTURE_TRIM_CHARS:
            tail = self.getvalue()[-_MAX_CAPTURE_CHARS:]
            newline = tail.find("\n")
            if newline >= 0:
                tail = tail[newline + 1 :]
            self.seek(0)
            self.truncate(0)
            super().write("[Earlier output trimmed]\n" + tail)
        return written


def _normalize_path(path):
    if not path:
        return None
    return os.path.abspath(path)


def _python_source_directories(root_directory):
    root_directory = _normalize_path(root_directory)
    if not root_directory or not os.path.isdir(root_directory):
        return []

    directories = []
    for current_directory, directory_names, filenames in os.walk(root_directory):
        directory_names[:] = [
            name
            for name in directory_names
            if name not in _IGNORED_SOURCE_DIRECTORIES
            and not os.path.isfile(os.path.join(current_directory, name, "pyvenv.cfg"))
        ]
        if any(filename.endswith(".py") for filename in filenames):
            directories.append(current_directory)
    return directories


def _module_file(module):
    filename = getattr(module, "__file__", None)
    if not filename:
        return None
    return _normalize_path(filename)


def _preload_virtual_environment_libraries(site_packages):
    library_root = os.path.join(site_packages, "chaquopy", "lib")
    if not os.path.isdir(library_root):
        return

    pending = []
    for current_directory, _, filenames in os.walk(library_root):
        for filename in filenames:
            if filename.endswith(".so"):
                path = os.path.join(current_directory, filename)
                if path not in _NATIVE_LIBRARY_HANDLES:
                    pending.append(path)

    # Shared libraries may appear before their dependencies in the directory
    # listing, so retry until no additional library can be loaded.
    while pending:
        remaining = []
        loaded_any = False
        for path in pending:
            try:
                handle = ctypes.CDLL(path, mode=getattr(ctypes, "RTLD_GLOBAL", 0))
            except OSError:
                remaining.append(path)
            else:
                _NATIVE_LIBRARY_HANDLES[path] = handle
                loaded_any = True
        if not loaded_any:
            break
        pending = remaining


def _is_inside(directory, filename):
    directory = _normalize_path(directory)
    filename = _normalize_path(filename)
    if not directory or not filename:
        return False
    try:
        return os.path.commonpath([directory, filename]) == directory
    except ValueError:
        return False


def _purge_workspace_modules(root_directory):
    root_directory = _normalize_path(root_directory)
    if not root_directory:
        return

    for name, module in list(sys.modules.items()):
        filename = _module_file(module)
        if filename and _is_inside(root_directory, filename):
            sys.modules.pop(name, None)


def _decode_arguments(arguments_json):
    if not arguments_json:
        return []
    try:
        arguments = json.loads(arguments_json)
    except Exception:
        return [str(arguments_json)]
    if not isinstance(arguments, list):
        return []
    return [str(argument) for argument in arguments]


def _is_cancelled(cancel_controller):
    if cancel_controller is None:
        return False
    try:
        return bool(cancel_controller.isStopped())
    except Exception:
        return False


def _cancel_trace(cancel_controller):
    def trace(frame, event, arg):
        if event in ("call", "line", "return") and _is_cancelled(cancel_controller):
            raise KeyboardInterrupt()
        return trace

    return trace


def _line_offsets(source):
    offsets = []
    position = 0
    for line in source.splitlines(keepends=True):
        offsets.append(position)
        position += len(line)
    if not offsets:
        offsets.append(0)
    return offsets


def _position_to_offset(offsets, line, column):
    line_index = max(0, min((line or 1) - 1, len(offsets) - 1))
    return offsets[line_index] + max(0, column or 0)


def _diagnostic(
    severity,
    message,
    line,
    column=0,
    end_line=None,
    end_column=None,
    source="PocketCodedPy",
):
    end_line = end_line or line
    end_column = end_column if end_column is not None else (column + 1)
    return {
        "severity": severity,
        "message": message,
        "line": max(1, line or 1),
        "column": max(0, column or 0),
        "endLine": max(1, end_line or line or 1),
        "endColumn": max(0, end_column or 0),
        "source": source,
    }


def _node_range(node):
    line = getattr(node, "lineno", 1)
    column = getattr(node, "col_offset", 0)
    end_line = getattr(node, "end_lineno", line)
    end_column = getattr(node, "end_col_offset", column + 1)
    return line, column, end_line, end_column


def _utf8_column_to_character(source_lines, line, column):
    if not source_lines:
        return max(0, column or 0)
    line_index = max(0, min((line or 1) - 1, len(source_lines) - 1))
    encoded = source_lines[line_index].encode("utf-8")
    byte_column = max(0, min(column or 0, len(encoded)))
    return len(encoded[:byte_column].decode("utf-8", errors="ignore"))


def get_completions(
    source,
    line,
    column,
    filename="main.py",
    working_directory=None,
    root_directory=None,
    virtual_environment=None,
):
    source = source or ""
    line = max(1, line or 1)
    column = max(0, column or 0)
    source_lines = source.splitlines()
    if not source_lines:
        source_lines = [""]
    
    # Identify the prefix and dot access
    current_line = source_lines[line - 1] if line <= len(source_lines) else ""
    prefix_start = column
    while prefix_start > 0 and (current_line[prefix_start - 1].isalnum() or current_line[prefix_start - 1] == "_"):
        prefix_start -= 1
    
    prefix = current_line[prefix_start:column]
    is_dot_access = prefix_start > 0 and current_line[prefix_start - 1] == "."
    
    completions = []
    
    try:
        # We run the completion in the same environment as code execution to pick up imports
        with _execution_environment(
            filename=filename,
            working_directory=working_directory,
            root_directory=root_directory,
            virtual_environment=virtual_environment,
        ) as captured:
            # Simple static analysis for local variables/functions/classes
            try:
                tree = ast.parse(source)
                for node in ast.walk(tree):
                    if isinstance(node, ast.FunctionDef):
                        completions.append({"label": f"{node.name}()", "insertText": f"{node.name}()", "detail": "function", "cursorOffset": len(node.name) + 1})
                    elif isinstance(node, ast.ClassDef):
                        completions.append({"label": node.name, "insertText": node.name, "detail": "class"})
                    elif isinstance(node, ast.Name) and isinstance(node.ctx, ast.Store):
                        completions.append({"label": node.id, "insertText": node.id, "detail": "variable"})
            except:
                pass

            # Dynamic analysis for imports and dot access
            scope = {
                "__name__": "__main__",
                "__builtins__": builtins.__dict__,
            }
            
            # Execute imports found in source to populate scope
            import_lines = [l for l in source_lines if l.strip().startswith(("import ", "from "))]
            for il in import_lines:
                try:
                    exec(il, scope)
                except:
                    pass
            
            if is_dot_access:
                # Find the target expression before the dot
                expr_end = prefix_start - 1
                expr_start = expr_end
                # Very simple expression parser for dot access (handles simple names and chains)
                parens = 0
                brackets = 0
                while expr_start > 0:
                    char = current_line[expr_start - 1]
                    if char == ")": parens += 1
                    elif char == "(": parens -= 1
                    elif char == "]": brackets += 1
                    elif char == "[": brackets -= 1
                    
                    if parens == 0 and brackets == 0:
                        if not (char.isalnum() or char in "_."):
                            break
                    expr_start -= 1
                
                target_expr = current_line[expr_start:expr_end].strip()
                if target_expr:
                    try:
                        # Evaluate target expression in our mock scope
                        target_obj = eval(target_expr, scope)
                        for name in dir(target_obj):
                            if name.startswith(prefix):
                                try:
                                    attr = getattr(target_obj, name)
                                    detail = type(attr).__name__
                                    if callable(attr):
                                        completions.append({"label": f"{name}()", "insertText": f"{name}()", "detail": detail, "cursorOffset": len(name) + 1})
                                    else:
                                        completions.append({"label": name, "insertText": name, "detail": detail})
                                except:
                                    completions.append({"label": name, "insertText": name, "detail": "attribute"})
                    except:
                        pass
            else:
                # Global scope completions (builtins + keywords + scope)
                for name in scope.keys():
                    if name.startswith(prefix) and not name.startswith("__"):
                        obj = scope[name]
                        detail = "module" if isinstance(obj, type(os)) else type(obj).__name__
                        if callable(obj):
                            completions.append({"label": f"{name}()", "insertText": f"{name}()", "detail": detail, "cursorOffset": len(name) + 1})
                        else:
                            completions.append({"label": name, "insertText": name, "detail": detail})
                
                # Builtins
                for name in dir(builtins):
                    if name.startswith(prefix) and not name.startswith("__"):
                        obj = getattr(builtins, name)
                        if callable(obj):
                            completions.append({"label": f"{name}()", "insertText": f"{name}()", "detail": "builtin", "cursorOffset": len(name) + 1})
                        else:
                            completions.append({"label": name, "insertText": name, "detail": "builtin"})

    except Exception as e:
        # Fallback to very basic completions on error
        pass

    # Deduplicate and filter
    seen = set()
    final_completions = []
    for c in completions:
        if c["label"] not in seen and c["label"].lower().startswith(prefix.lower()):
            final_completions.append(c)
            seen.add(c["label"])
            
    return json.dumps({"completions": final_completions[:50]})


def inspect_code(source, filename="main.py"):
    source = source or ""
    diagnostics = []
    source_lines = source.splitlines(keepends=True) or [""]

    try:
        tokens = list(tokenize.generate_tokens(io.StringIO(source).readline))
    except tokenize.TokenError as error:
        message, location = error.args
        line, column = location
        diagnostics.append(_diagnostic("error", message, line, column, source="tokenize"))
        return json.dumps({"diagnostics": diagnostics})

    try:
        tree = ast.parse(source, filename=filename or "main.py")
    except SyntaxError as error:
        diagnostics.append(
            _diagnostic(
                "error",
                error.msg,
                error.lineno or 1,
                max(0, (error.offset or 1) - 1),
                error.end_lineno or error.lineno or 1,
                max(0, (error.end_offset or error.offset or 1) - 1),
                source="syntax",
            )
        )
        return json.dumps({"diagnostics": diagnostics})

    defined = set(dir(builtins)) | {
        "__name__",
        "__file__",
        "__package__",
        "self",
        "cls",
        "True",
        "False",
        "None",
    }
    imports = {}
    loaded_names = []

    class Inspector(ast.NodeVisitor):
        def _define_target(self, target):
            if isinstance(target, ast.Name):
                defined.add(target.id)
            elif isinstance(target, (ast.Tuple, ast.List)):
                for element in target.elts:
                    self._define_target(element)

        def visit_Import(self, node):
            for alias in node.names:
                name = alias.asname or alias.name.split(".")[0]
                defined.add(name)
                imports[name] = node

        def visit_ImportFrom(self, node):
            for alias in node.names:
                if alias.name == "*":
                    continue
                name = alias.asname or alias.name
                defined.add(name)
                imports[name] = node

        def visit_FunctionDef(self, node):
            defined.add(node.name)
            for argument in (
                list(node.args.posonlyargs)
                + list(node.args.args)
                + list(node.args.kwonlyargs)
            ):
                defined.add(argument.arg)
            if node.args.vararg:
                defined.add(node.args.vararg.arg)
            if node.args.kwarg:
                defined.add(node.args.kwarg.arg)
            if len(node.args.defaults) > 0:
                defaults = node.args.defaults + node.args.kw_defaults
                for default in defaults:
                    if isinstance(default, (ast.List, ast.Dict, ast.Set)):
                        line, col, end_line, end_col = _node_range(default)
                        diagnostics.append(
                            _diagnostic(
                                "warning",
                                "Mutable default argument keeps state between calls.",
                                line,
                                col,
                                end_line,
                                end_col,
                                source="inspection",
                            )
                        )
            self.generic_visit(node)

        def visit_AsyncFunctionDef(self, node):
            self.visit_FunctionDef(node)

        def visit_ClassDef(self, node):
            defined.add(node.name)
            self.generic_visit(node)

        def visit_Assign(self, node):
            for target in node.targets:
                self._define_target(target)
            self.generic_visit(node)

        def visit_AnnAssign(self, node):
            self._define_target(node.target)
            self.generic_visit(node)

        def visit_AugAssign(self, node):
            self._define_target(node.target)
            self.generic_visit(node)

        def visit_For(self, node):
            self._define_target(node.target)
            self.generic_visit(node)

        def visit_With(self, node):
            for item in node.items:
                if item.optional_vars:
                    self._define_target(item.optional_vars)
            self.generic_visit(node)

        def visit_ExceptHandler(self, node):
            if node.name:
                defined.add(node.name)
            if node.type is None:
                line, col, end_line, end_col = _node_range(node)
                diagnostics.append(
                    _diagnostic(
                        "warning",
                        "Bare except catches every error. Catch a specific exception type.",
                        line,
                        col,
                        end_line,
                        end_col,
                        source="inspection",
                    )
                )
            self.generic_visit(node)

        def visit_Name(self, node):
            if isinstance(node.ctx, ast.Store):
                defined.add(node.id)
            elif isinstance(node.ctx, ast.Load):
                loaded_names.append(node)
            self.generic_visit(node)

        def visit_Compare(self, node):
            for operator, comparator in zip(node.ops, node.comparators):
                if isinstance(comparator, ast.Constant) and comparator.value is None:
                    if isinstance(operator, (ast.Eq, ast.NotEq)):
                        line, col, end_line, end_col = _node_range(node)
                        diagnostics.append(
                            _diagnostic(
                                "hint",
                                "Use 'is None' or 'is not None' when comparing with None.",
                                line,
                                col,
                                end_line,
                                end_col,
                                source="inspection",
                            )
                        )
            self.generic_visit(node)

        def visit_BinOp(self, node):
            if isinstance(node.op, (ast.Div, ast.FloorDiv, ast.Mod)):
                if isinstance(node.right, ast.Constant) and node.right.value == 0:
                    line, col, end_line, end_col = _node_range(node.right)
                    diagnostics.append(
                        _diagnostic(
                            "error",
                            "This expression divides by zero at runtime.",
                            line,
                            col,
                            end_line,
                            end_col,
                            source="inspection",
                        )
                    )
            self.generic_visit(node)

    Inspector().visit(tree)

    used = {node.id for node in loaded_names}
    for name, node in imports.items():
        if name not in used:
            line, col, end_line, end_col = _node_range(node)
            diagnostics.append(
                _diagnostic(
                    "warning",
                    f"Imported name '{name}' is not used.",
                    line,
                    col,
                    end_line,
                    end_col,
                    source="inspection",
                )
            )

    for node in loaded_names:
        if node.id not in defined:
            line, col, end_line, end_col = _node_range(node)
            diagnostics.append(
                _diagnostic(
                    "error",
                    f"'{node.id}' may be undefined.",
                    line,
                    col,
                    end_line,
                    end_col,
                    source="inspection",
                )
            )

    for diagnostic in diagnostics:
        diagnostic["column"] = _utf8_column_to_character(
            source_lines,
            diagnostic["line"],
            diagnostic["column"],
        )
        diagnostic["endColumn"] = _utf8_column_to_character(
            source_lines,
            diagnostic["endLine"],
            diagnostic["endColumn"],
        )

    return json.dumps({"diagnostics": diagnostics[:60]})


def _flush_capture(stdout, stderr):
    output = stdout.getvalue()
    error = stderr.getvalue()
    stdout.seek(0)
    stdout.truncate(0)
    stderr.seek(0)
    stderr.truncate(0)
    return output, error


@contextlib.contextmanager
def _execution_environment(
    stdin_text="",
    filename="main.py",
    working_directory=None,
    root_directory=None,
    virtual_environment=None,
    argv=None,
    input_callback=None,
    cancel_controller=None,
):
    stdout = _CappedStringIO()
    stderr = _CappedStringIO()
    stdin = io.StringIO(stdin_text or "")
    filename = _normalize_path(filename) or "main.py"
    script_directory = _normalize_path(os.path.dirname(filename))
    root_directory = _normalize_path(root_directory) or script_directory
    working_directory = _normalize_path(working_directory) or script_directory or root_directory
    virtual_environment = _normalize_path(virtual_environment)
    if virtual_environment and not os.path.isfile(os.path.join(virtual_environment, "pyvenv.cfg")):
        virtual_environment = None

    old_input = builtins.input
    old_cwd = os.getcwd()
    old_sys_path = list(sys.path)
    old_argv = list(sys.argv)
    old_trace = sys.gettrace()
    old_virtual_env = os.environ.get("VIRTUAL_ENV")
    old_path = os.environ.get("PATH")
    old_prefix = sys.prefix
    old_exec_prefix = sys.exec_prefix

    def terminal_input(prompt=""):
        if _is_cancelled(cancel_controller):
            raise KeyboardInterrupt()
        if prompt:
            print(prompt, end="")
        line = stdin.readline()
        if line == "" and input_callback is not None:
            output, error = _flush_capture(stdout, stderr)
            line = input_callback.requestInput(output, error)
            if _is_cancelled(cancel_controller):
                raise KeyboardInterrupt()
            if line is None:
                raise EOFError("No terminal input available")
            line = str(line)
        if line == "":
            raise EOFError("No terminal input available")
        return line.rstrip("\n")

    try:
        builtins.input = terminal_input
        sys.argv = list(argv or [filename])
        if working_directory and os.path.isdir(working_directory):
            os.chdir(working_directory)

        import_paths = []
        if virtual_environment:
            environment_site_packages = os.path.join(
                virtual_environment,
                "lib",
                "python",
                "site-packages",
            )
            os.makedirs(environment_site_packages, exist_ok=True)
            import_paths.append(environment_site_packages)
            os.environ["VIRTUAL_ENV"] = virtual_environment
            environment_bin = os.path.join(virtual_environment, "bin")
            os.environ["PATH"] = environment_bin + os.pathsep + (old_path or "")
            sys.prefix = virtual_environment
            sys.exec_prefix = virtual_environment
            _preload_virtual_environment_libraries(environment_site_packages)
        for path in [script_directory, working_directory, root_directory]:
            if path and os.path.isdir(path) and path not in import_paths:
                import_paths.append(path)
        for path in _python_source_directories(root_directory):
            if path not in import_paths:
                import_paths.append(path)

        _purge_workspace_modules(root_directory)
        sys.path[:0] = [path for path in import_paths if path not in sys.path]
        if cancel_controller is not None:
            sys.settrace(_cancel_trace(cancel_controller))

        with contextlib.redirect_stdout(stdout), contextlib.redirect_stderr(stderr):
            yield stdout, stderr, filename
    finally:
        sys.settrace(old_trace)
        builtins.input = old_input
        os.chdir(old_cwd)
        sys.path[:] = old_sys_path
        sys.argv[:] = old_argv
        sys.prefix = old_prefix
        sys.exec_prefix = old_exec_prefix
        if old_virtual_env is None:
            os.environ.pop("VIRTUAL_ENV", None)
        else:
            os.environ["VIRTUAL_ENV"] = old_virtual_env
        if old_path is None:
            os.environ.pop("PATH", None)
        else:
            os.environ["PATH"] = old_path


def _result_json(success, stdout=None, stderr=None, error_text="", stopped=False):
    return json.dumps(
        {
            "success": success,
            "output": stdout.getvalue() if stdout else "",
            "error": (stderr.getvalue() if stderr else "") + error_text,
            "stopped": stopped,
        }
    )


def run_code(
    source,
    stdin_text="",
    filename="main.py",
    working_directory=None,
    root_directory=None,
    virtual_environment=None,
    input_callback=None,
    cancel_controller=None,
):
    stdout = None
    stderr = None
    try:
        with _execution_environment(
            stdin_text,
            filename,
            working_directory,
            root_directory,
            virtual_environment,
            input_callback=input_callback,
            cancel_controller=cancel_controller,
        ) as captured:
            stdout, stderr, filename = captured
            globals_dict = {
                "__name__": "__main__",
                "__file__": filename,
                "__package__": None,
                "__builtins__": builtins.__dict__,
            }
            exec(compile(source or "", filename, "exec"), globals_dict, globals_dict)
        return _result_json(True, stdout, stderr)
    except KeyboardInterrupt:
        return _result_json(False, stdout, stderr, stopped=True)
    except BaseException:
        return _result_json(False, stdout, stderr, traceback.format_exc())


def run_unittest(
    arguments_json="[]",
    working_directory=None,
    root_directory=None,
    virtual_environment=None,
    cancel_controller=None,
):
    stdout = None
    stderr = None
    arguments = _decode_arguments(arguments_json)
    argv = ["unittest"] + arguments
    filename = os.path.join(working_directory or root_directory or "", "unittest")

    try:
        with _execution_environment(
            stdin_text="",
            filename=filename,
            working_directory=working_directory,
            root_directory=root_directory,
            virtual_environment=virtual_environment,
            argv=argv,
            cancel_controller=cancel_controller,
        ) as captured:
            stdout, stderr, _ = captured
            program = unittest.main(
                module=None,
                argv=argv,
                exit=False,
                testRunner=unittest.TextTestRunner(stream=stderr),
            )
            success = program.result.wasSuccessful() if program.result is not None else True
        return _result_json(success, stdout, stderr)
    except SystemExit as error:
        code = error.code
        success = code is None or code == 0
        return _result_json(success, stdout, stderr)
    except KeyboardInterrupt:
        return _result_json(False, stdout, stderr, stopped=True)
    except Exception:
        return _result_json(False, stdout, stderr, traceback.format_exc())
