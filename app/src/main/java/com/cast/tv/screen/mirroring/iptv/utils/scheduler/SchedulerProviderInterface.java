package com.cast.tv.screen.mirroring.iptv.utils.scheduler;

import io.reactivex.Scheduler;

public interface SchedulerProviderInterface {

    Scheduler computation();

    Scheduler io();

    Scheduler ui();
}
