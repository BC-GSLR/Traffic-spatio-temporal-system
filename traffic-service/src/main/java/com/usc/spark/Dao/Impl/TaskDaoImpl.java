package com.usc.spark.Dao.Impl;

import com.usc.spark.Dao.TaskDao;
import com.usc.spark.jdbc.JdbcHelper;
import com.usc.spark.pojo.Task;

import java.sql.ResultSet;
import com.usc.spark.jdbc.JdbcHelper.QueryCallback;
public class TaskDaoImpl implements TaskDao {
    @Override
    public Task findTaskById(String taskId) {
        Task task = new Task();
        String sql = "select * from task where task_id=?";
        Object[] params = new Object[]{taskId};
        JdbcHelper jdbcHelper = JdbcHelper.getInstance();
        jdbcHelper.executeQuery(sql, params, new QueryCallback() {

            @Override
            public void process(ResultSet rs) throws Exception {
                if (rs.next()) {
                    long taskid = rs.getLong(1);
                    String taskName = rs.getString(2);
                    String createTime = rs.getString(3);
                    String startTime = rs.getString(4);
                    String finishTime = rs.getString(5);
                    String taskType = rs.getString(6);
                    String taskStatus = rs.getString(7);
                    String taskParam = rs.getString(8);//15

                    task.setTaskId(String.valueOf(taskid));
                    task.setTaskName(taskName);
                    task.setCreateTime(createTime);
                    task.setStartTime(startTime);
                    task.setFinishTime(finishTime);
                    task.setTaskType(taskType);
                    task.setTaskStatus(taskStatus);
                    task.setTaskParams(taskParam);
                }
            }
        });
        return task;
    }
}