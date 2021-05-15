package com.ling.video;

import android.opengl.GLSurfaceView.EGLConfigChooser;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class CustomChooseConfig {
    public CustomChooseConfig() {
    }

    public static class SimpleEGLConfigChooser extends CustomChooseConfig.ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0);
        }
    }

    public static class ComponentSizeChooser extends CustomChooseConfig.BaseConfigChooser {
        private int[] mValue = new int[1];
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{12324, redSize, 12323, greenSize, 12322, blueSize, 12321, alphaSize, 12325, depthSize, 12326, stencilSize, 12344});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            EGLConfig[] var4 = configs;
            int var5 = configs.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                EGLConfig config = var4[var6];
                int d = this.findConfigAttrib(egl, display, config, 12325, 0);
                int s = this.findConfigAttrib(egl, display, config, 12326, 0);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = this.findConfigAttrib(egl, display, config, 12324, 0);
                    int g = this.findConfigAttrib(egl, display, config, 12323, 0);
                    int b = this.findConfigAttrib(egl, display, config, 12322, 0);
                    int a = this.findConfigAttrib(egl, display, config, 12321, 0);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }

            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            return egl.eglGetConfigAttrib(display, config, attribute, this.mValue) ? this.mValue[0] : defaultValue;
        }
    }

    public abstract static class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = this.filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, this.mConfigSpec, (EGLConfig[])null, 0, num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            } else {
                int numConfigs = num_config[0];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                } else {
                    EGLConfig[] configs = new EGLConfig[numConfigs];
                    if (!egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                        throw new IllegalArgumentException("eglChooseConfig#2 failed");
                    } else {
                        EGLConfig config = this.chooseConfig(egl, display, configs);
                        if (config == null) {
                            throw new IllegalArgumentException("No config chosen");
                        } else {
                            return config;
                        }
                    }
                }
            }
        }

        abstract EGLConfig chooseConfig(EGL10 var1, EGLDisplay var2, EGLConfig[] var3);

        private int[] filterConfigSpec(int[] configSpec) {
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = 12352;
            newConfigSpec[len] = 4;
            newConfigSpec[len + 1] = 12344;
            return newConfigSpec;
        }
    }
}
