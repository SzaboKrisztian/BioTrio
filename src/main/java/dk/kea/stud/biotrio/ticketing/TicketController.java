package dk.kea.stud.biotrio.ticketing;

import dk.kea.stud.biotrio.AppGlobals;
import dk.kea.stud.biotrio.cinema.ScreeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Defines the routes related to {@link Ticket} management
 */
@Controller
public class TicketController {
  @Autowired
  private TicketRepository ticketRepo;
  @Autowired
  private ScreeningRepository screeningRepo;
  @Autowired
  private SeatRepository seatRepo;
  @Autowired
  private BookingRepository bookingRepo;


  /**
   * Displays the manage upcoming screenings list view
   */
  @GetMapping("/manage/ticketing")
  public String screeningsForBookingOrSale(Model model){
    model.addAttribute("upcomingScreenings", screeningRepo.findUpcomingScreeningsAsMap());
    return "ticketing/ticketing";
  }

  @GetMapping("/manage/ticketing/{id}")
  public String screeningDetailView(@PathVariable int id,
                                    Model model) {
    model.addAttribute("screening", screeningRepo.findById(id));
    return "ticketing/screening-detail-view";
  }

  /**
   * Displays the add ticket view for selected screening
   */
  @GetMapping("/manage/screening/{screeningId}/ticketing")
  public String screeningTicketing(@PathVariable(name = "screeningId") int id, Model model) {
    SeatData data = new SeatData();
    data.setSeats(seatRepo.getSeatStatusForScreening(id));
    data.setSubmittedData(new ArrayList<>());
    for (Seat seat : data.getSeats()) {
      if (seat.isSold()) {
        data.getSubmittedData().add("" + seat.getRowNo() + "_" + seat.getSeatNo());
      } else if (screeningRepo.findById(id).getStartTime().isBefore(LocalDateTime.now().plusMinutes(30))){
          data.getSubmittedData().add("");
          seat.setAvailable(true);
          bookingRepo.deleteBookingsForScreening(id);
      }
        else
          data.getSubmittedData();
    }
    model.addAttribute("data", data);
    model.addAttribute("screening", screeningRepo.findById(id)); 
    return "ticketing/ticketing-id-add";
  }

  /**
   * Converts the data received from the add ticket view and adds the tickets data
   * to the database, then redirects the user to the add ticket view
   */
  @PostMapping("/manage/screening/{screeningId}/ticketing")
  public String screeningTicketing(@PathVariable(name = "screeningId") int id,
                                   @ModelAttribute SeatData data) {
    for (Seat seat:seatRepo.convertStringSeatData(data.getSubmittedData())) {
      Ticket ticket = new Ticket();
      ticket.setScreening(screeningRepo.findById(id));
      ticket.setSeat(seat);
      ticketRepo.addTicket(ticket);
      AppGlobals.printTicket(ticket);
    }
    return "redirect:/manage/ticketing/" + id;
  }

  /**
   * Displays the void ticket view for selected screening
   */
  @GetMapping("/manage/screening/{screeningId}/ticketing/void")
  public String deleteTicket(@PathVariable(name = "screeningId") int id, Model model) {
    SeatData data = new SeatData();
    data.setSeats(seatRepo.getSeatStatusForScreening(id));
    data.setSubmittedData(new ArrayList<>());
    model.addAttribute("data", data);
    model.addAttribute("screening", screeningRepo.findById(id));
    return "ticketing/ticketing-void";
  }

  /**
   * Converts the data received from the void ticket view and deletes the tickets data
   * from the database, then redirects the user the add ticket view
   */
  @PostMapping("/manage/screening/{screeningId}/ticketing/void")
  public String deleteTicket(@PathVariable(name = "screeningId") int id,
                             @ModelAttribute SeatData data) {
    for (Seat seat : seatRepo.convertStringSeatData(data.getSubmittedData())) {
      Ticket ticket = new Ticket();
      ticket.setScreening(screeningRepo.findById(id));
      ticket.setSeat(seat);
      ticketRepo.deleteTicket(ticket);
    }
    return "redirect:/manage/ticketing/" + id;
  }



}
