package com.ling.video;

import android.os.ConditionVariable;

public abstract class WorkThread extends Thread {
    protected volatile boolean mIsRunning = false;
    ConditionVariable mConditionVariable = new ConditionVariable();

    public WorkThread(String name) {
        super(name);
        this.setPriority(1);
    }

    public void run() {
        this.mConditionVariable.close();
        this.doInitial();

        while(this.mIsRunning) {
            try {
                this.doRepeatWork();
            } catch (IllegalStateException var2) {
                var2.printStackTrace();
            } catch (Throwable var3) {
                var3.printStackTrace();
            }
        }

        this.doRelease();
        this.mConditionVariable.open();
    }

    protected abstract int doRepeatWork() throws InterruptedException;

    protected abstract void doInitial();

    protected abstract void doRelease();

    public synchronized void start() {
        if (!this.mIsRunning) {
            this.mIsRunning = true;
            super.start();
        }
    }

    public synchronized void stopThreadAsyn() {
        this.mIsRunning = false;
        this.interrupt();
    }

    public synchronized void stopThreadSyn() {
        if (this.mIsRunning) {
            this.mIsRunning = false;
            this.mConditionVariable.block(2000L);
        }
    }

    public synchronized boolean isRunning() {
        return this.mIsRunning;
    }
}
