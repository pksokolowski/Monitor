package com.example.sokol.monitor;

import com.example.sokol.monitor.LogsDialog.Log;

import java.util.List;

public interface LogsProvider {
    List<Log> getLogs();
}
