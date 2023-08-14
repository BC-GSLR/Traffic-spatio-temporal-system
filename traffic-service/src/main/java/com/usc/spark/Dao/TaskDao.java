package com.usc.spark.Dao;

import com.usc.spark.pojo.Task;

public interface TaskDao {
    Task findTaskById(String taskId);
}
