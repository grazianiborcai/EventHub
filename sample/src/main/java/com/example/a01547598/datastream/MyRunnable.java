package com.example.a01547598.datastream;

/**
 * Created by 01547598 on 1/26/2017.
 */

abstract class MyRunnable implements Runnable
{

    private boolean killMe = false;

    public abstract void doWork();

    @Override
    public void run() {
        if(killMe)
            return;

        doWork();
    }

    public void killRunnable()
    {
        killMe = true;
    }

    public void liveRunnable()
    {
        killMe = false;
    }
}
