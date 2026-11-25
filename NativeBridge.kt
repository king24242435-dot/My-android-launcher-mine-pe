package com.solauncher.bedrock

object NativeBridge {
    init {
        System.loadLibrary("solauncher")
    }

    external fun nativeLoadLibrary(libPath: String): Boolean
    external fun nativeGetLastError(): String
}
