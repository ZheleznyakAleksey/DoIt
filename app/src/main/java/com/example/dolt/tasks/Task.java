package com.example.dolt.tasks;

public class Task implements Comparable<Task>{
    private String taskText, taskId, userFrom, userFromId, userTo, userToId, toOrFrom, termDateTime;
    private int degreeOfImportance, taskStatus, isTaskCheck;
    private boolean checked;

    public Task(String taskText, String taskId, String userFrom, String userFromId, String userTo, String userToId, String toOrFrom, int taskStatus, int degreeOfImportance, String termDateTime, int isTaskCheck) {
        this.taskText = taskText;
        this.taskId = taskId;
        this.userFrom = userFrom;
        this.userFromId = userFromId;
        this.userTo = userTo;
        this.userToId = userToId;
        this.toOrFrom = toOrFrom;
        this.taskStatus = taskStatus;
        this.degreeOfImportance = degreeOfImportance;
        this.termDateTime = termDateTime;
        this.isTaskCheck = isTaskCheck;
        this.checked = taskStatus == 4;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getToOrFrom() {
        return toOrFrom;
    }

    public void setToOrFrom(String toOrFrom) {
        this.toOrFrom = toOrFrom;
    }

    public String getTaskText() {
        return taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public String getUserFrom() {
        return userFrom;
    }

    public void setUserFrom(String userFrom) {
        this.userFrom = userFrom;
    }

    public String getUserFromId() {
        return userFromId;
    }

    public void setUserFromId(String userFromId) {
        this.userFromId = userFromId;
    }

    public String getUserTo() {
        return userTo;
    }

    public void setUserTo(String userTo) {
        this.userTo = userTo;
    }

    public String getUserToId() {
        return userToId;
    }

    public int getDegreeOfImportance() {
        return degreeOfImportance;
    }

    public void setDegreeOfImportance(int degreeOfImportance) {
        this.degreeOfImportance = degreeOfImportance;
    }

    public String getTermDateTime() {
        return termDateTime;
    }

    public void setTermDateTime(String termDateTime) {
        this.termDateTime = termDateTime;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setUserToId(String userToId) {
        this.userToId = userToId;
    }

    public int getIsTaskCheck() {
        return isTaskCheck;
    }

    public void setIsTaskCheck(int isTaskCheck) {
        this.isTaskCheck = isTaskCheck;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public int compareTo(Task task) {
        if (this.taskStatus-task.taskStatus==0)
            return task.degreeOfImportance-this.degreeOfImportance;
        else {
            int thisTaskStatus = this.getTaskStatus();
            int taskTaskStatus = task.getTaskStatus();
            if ((thisTaskStatus==0 || thisTaskStatus==2) && (taskTaskStatus==1 || taskTaskStatus==3 || taskTaskStatus==4)) return -1;
            else if ((taskTaskStatus==0 || taskTaskStatus==2) && (thisTaskStatus==1 || thisTaskStatus==3 || thisTaskStatus==4)) return 1;
            else  {
                return this.taskStatus-task.taskStatus;
            }

        }
    }
}
