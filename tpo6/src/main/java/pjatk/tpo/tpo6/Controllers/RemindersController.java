package pjatk.tpo.tpo6.Controllers;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pjatk.tpo.tpo6.Models.ReminderModel;
import pjatk.tpo.tpo6.Services.RemindersService;


import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reminder")
public class RemindersController {
    private final RemindersService remindersService;

    public RemindersController(RemindersService remindersService) {
        this.remindersService = remindersService;
    }

// === Thymeleaf form ===

    //Default get
    @GetMapping
    public String getAllReminders(Model model) {
        model.addAttribute("reminders", remindersService.getReminders());
        model.addAttribute("editingId", null);
        return "reminders";
    }

    //Get completed reminders
    @GetMapping("/completed")
    public String getCompleted(Model model) {
        model.addAttribute("reminders", remindersService.getCompletedReminders());
        return "reminders";
    }

    //Post new reminder from input forms
    @PostMapping
    public String createReminderForm(
            @RequestParam("Name") String name,
            @RequestParam("Description") String description,
            @RequestParam("Date") String date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        ReminderModel reminder = new ReminderModel(0, name, description, localDateTime, false);
        remindersService.insertReminder(reminder);
        return "redirect:/reminder";
    }

    //Post 'completed' update on reminder
    @PostMapping("/toggle/{id}")
    public String toggleCompleted(@PathVariable int id) {
        remindersService.toggleCompleted(id);
        return "redirect:/reminder";
    }

    //Post edit request for reminder
    @PostMapping("/edit/{id}")
    public String startEditing(@PathVariable int id, Model model) {
        model.addAttribute("reminders", remindersService.getReminders());
        model.addAttribute("editingId", id);
        return "reminders";
    }

    //Put updated reminder
    @PutMapping("/{id}")
    public String updateReminder(@PathVariable int id,
     @RequestParam("Name") String name,
     @RequestParam("Description") String description,
     @RequestParam("Date") LocalDateTime date)  {
        ReminderModel reminder = remindersService.getReminder(id);
        reminder.setName(name);
        reminder.setDescription(description);
        reminder.setDate(date);
        remindersService.updateReminder(reminder);
        return "redirect:/reminder";
    }

    //Delete reminder
    @DeleteMapping("/{id}")
    public String deleteReminder(@PathVariable int id) {
        remindersService.deleteReminder(id);
        return "redirect:/reminder";
    }

// === Rest api "/data" uses JSON ===

    //Get all reminder
    @GetMapping("/data/all")
    @ResponseBody
    public List<ReminderModel> getRemindersJson() {
        return remindersService.getReminders();
    }

    //Post new reminder
    @PostMapping("/data")
    @ResponseBody
    public ResponseEntity<ReminderModel> createReminderJson(@RequestBody ReminderModel reminder) {
        ReminderModel createdReminder = remindersService.insertReminder(reminder);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdReminder.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdReminder);
    }

    //Put edited reminder
    @PutMapping("/data")
    @ResponseBody
    public ResponseEntity<ReminderModel> updateReminderJson(@RequestBody ReminderModel reminder) {
        boolean updated = remindersService.updateReminder(reminder);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    //Delete reminder
    @DeleteMapping("/data/{id}")
    public ResponseEntity<String> deleteReminderApi(@PathVariable int id) {
        boolean deleted = remindersService.deleteReminder(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }



}


