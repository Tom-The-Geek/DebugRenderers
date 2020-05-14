package me.geek.tom.debugrenderers.utils.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MappedMethod {

    private Method cache;
    private Class<?> cls;
    private List<Class<?>> argTypes;
    private String mcp, srg;

    private MappedMethod(Class<?> cls, String mcp, String srg, List<Class<?>> argTypes) {
        this.cls = cls;
        this.mcp = mcp;
        this.srg = srg;
        this.argTypes = argTypes;
    }

    public void call(Object obj, Object... args) {
        ensureCacheIsPopulated();
        try {
            this.cache.invoke(obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void ensureCacheIsPopulated() {
        if (cache != null) return;
        if (!tryGetMcp())
            if (!tryGetSrg())
                throw new IllegalStateException("Method does not exist! (MCP:" + mcp + ", SRG: " + srg + ")");
        ensureCacheAccessable();
    }

    private void ensureCacheAccessable() {
        cache.setAccessible(true);
    }

    private boolean tryGetMcp() {
        try {
            if (this.argTypes.size() != 0) {
                Class<?>[] args = new Class<?>[this.argTypes.size()];
                this.argTypes.toArray(args);
                cache = cls.getDeclaredMethod(mcp, args);
            } else
                cache = cls.getDeclaredMethod(mcp);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean tryGetSrg() {
        try {
            if (this.argTypes.size() != 0) {
                Class<?>[] args = new Class<?>[this.argTypes.size()];
                this.argTypes.toArray(args);
                cache = cls.getDeclaredMethod(srg, args);
            } else
                cache = cls.getDeclaredMethod(srg);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static class Builder {
        private Class<?> cls;
        private List<Class<?>> argTypes;
        private String mcp, srg;

        public Builder(Class<?> cls) {
            this.cls = cls;
            this.argTypes = new ArrayList<>();
            this.mcp = this.srg = "undefined";
        }

        public Builder mcp(String mcp) {
            this.mcp = mcp;
            return this;
        }

        public Builder srg(String srg) {
            this.srg = srg;
            return this;
        }

        public Builder arg(Class<?> arg) {
            this.argTypes.add(arg);
            return this;
        }

        public MappedMethod build() {
            return new MappedMethod(cls, mcp, srg, argTypes);
        }
    }

}
