/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sshd.common.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.ExecutionException;

/**
 * Android-compatible copy of Apache SSHD's ExceptionUtils.
 *
 * Apache SSHD 2.12.1 imports javax.management.MBeanException and
 * javax.management.ReflectionException even though those classes are not part
 * of Android. Loading that class crashes the SSH transport before clone/pull
 * can authenticate. This copy removes only the JMX-specific peeling branch.
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    public static void rethrowAsIoException(Throwable e) throws IOException {
        if (e instanceof IOException) {
            throw (IOException) e;
        } else if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else if (e instanceof Error) {
            throw (Error) e;
        } else {
            throw new IOException(e);
        }
    }

    public static <T extends Throwable> T accumulateException(T current, T extra) {
        if (current == null) {
            return extra;
        }

        if ((extra == null) || (extra == current)) {
            return current;
        }

        current.addSuppressed(extra);
        return current;
    }

    public static Throwable resolveExceptionCause(Throwable t) {
        if (t == null) {
            return t;
        }

        Throwable c = t.getCause();
        if (c == null) {
            return t;
        } else {
            return c;
        }
    }

    public static Throwable peelException(Throwable t) {
        if (t == null) {
            return t;
        } else if (t instanceof UndeclaredThrowableException) {
            Throwable wrapped = ((UndeclaredThrowableException) t).getUndeclaredThrowable();
            if (wrapped != null) {
                return peelException(wrapped);
            }

            wrapped = t.getCause();
            if (wrapped != t) {
                return peelException(wrapped);
            }
        } else if (t instanceof InvocationTargetException) {
            Throwable target = ((InvocationTargetException) t).getTargetException();
            if (target != null) {
                return peelException(target);
            }
        } else if (t instanceof ExecutionException) {
            return peelException(resolveExceptionCause(t));
        }

        return t;
    }

    public static RuntimeException toRuntimeException(Throwable t, boolean peelThrowable) {
        Throwable e = peelThrowable ? peelException(t) : t;
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }

        return new RuntimeException(e);
    }

    public static RuntimeException toRuntimeException(Throwable t) {
        return toRuntimeException(t, true);
    }
}
