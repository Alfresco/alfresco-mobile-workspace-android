package com.alfresco.content.viewer.pdf

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20

object EglExt {
    val maxTextureSize by lazy {
        val dpy: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(dpy, version, 0, version, 1)

        val configAttr = intArrayOf(
            EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
            EGL14.EGL_LEVEL, 0,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
            EGL14.EGL_NONE,
        )
        val configs: Array<EGLConfig?> = arrayOfNulls(1)
        val numConfig = IntArray(1)
        EGL14.eglChooseConfig(
            dpy,
            configAttr,
            0,
            configs,
            0,
            1,
            numConfig,
            0,
        )
        if (numConfig[0] == 0) {
            // TROUBLE! No config found.
            return@lazy 0
        }
        val config: EGLConfig? = configs[0]

        val surfAttr = intArrayOf(
            EGL14.EGL_WIDTH,
            64,
            EGL14.EGL_HEIGHT,
            64,
            EGL14.EGL_NONE,
        )
        val surf: EGLSurface = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0)

        val ctxAttrib = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            2,
            EGL14.EGL_NONE,
        )
        val ctx: EGLContext =
            EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0)

        EGL14.eglMakeCurrent(dpy, surf, surf, ctx)

        val maxSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxSize, 0)

        EGL14.eglMakeCurrent(
            dpy,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT,
        )
        EGL14.eglDestroySurface(dpy, surf)
        EGL14.eglDestroyContext(dpy, ctx)
        EGL14.eglTerminate(dpy)

        maxSize[0]
    }
}
