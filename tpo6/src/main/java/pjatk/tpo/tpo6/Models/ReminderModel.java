package pjatk.tpo.tpo6.Models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ReminderModel {

    private int id;
    private String name;
    private String description;
    private LocalDateTime date;
    private boolean completed;

    public ReminderModel(int id, String name, String description, LocalDateTime date, boolean completed) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.completed = completed;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isPastDue() {
        if (completed) return false;
        return this.date.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}
