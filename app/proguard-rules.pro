# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# WebView invokes these methods from JavaScript, so they are not visible to
# R8's normal static analysis.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep line numbers for readable de-obfuscated release crash reports while
# avoiding disclosure of source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optional JVM management and security-provider integrations referenced by
# JGit/Apache SSHD are not available (or used) on Android.
-dontwarn java.lang.management.ManagementFactory
-dontwarn javax.security.auth.login.CredentialException
-dontwarn javax.security.auth.login.FailedLoginException
-dontwarn javax.management.**
-dontwarn org.bouncycastle.asn1.pkcs.PrivateKeyInfo
-dontwarn org.bouncycastle.crypto.prng.RandomGenerator
-dontwarn org.bouncycastle.crypto.prng.VMPCRandomGenerator
-dontwarn org.bouncycastle.operator.InputDecryptorProvider
-dontwarn org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
-dontwarn org.bouncycastle.pkcs.PKCSException
-dontwarn org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder
-dontwarn org.bouncycastle.pqc.crypto.mlkem.MLKEMParameters
-dontwarn org.bouncycastle.pqc.crypto.ntruprime.SNTRUPrimeParameters

# SLF4J supports running without a concrete logger binding.
-dontwarn org.slf4j.impl.StaticLoggerBinder
# JGit advertises optional desktop PID-lock and Kerberos/GSS integrations which
# are absent on Android. PocketCoded Web uses HTTP token auth and Apache SSHD keys.
-dontwarn java.lang.ProcessHandle
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid
