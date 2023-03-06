package com.project.train_web_application.controllers;

import com.project.train_web_application.Models.PriceByDistance;
import com.project.train_web_application.Models.User;
import com.project.train_web_application.Models.Voyage;
import com.project.train_web_application.repositories.StationRepository;
import com.project.train_web_application.repositories.TrainRepository;
import com.project.train_web_application.repositories.VoyageRepository;
import com.project.train_web_application.services.UserService.UserService;
import com.project.train_web_application.services.roleService.RoleService;
import com.project.train_web_application.services.voyageService.VoyageService;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.sql.CallableStatement;
import java.sql.Types;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookingController {
    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VoyageRepository voyageRepository;


    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VoyageService voyageService;

    @GetMapping("/Responsable_reservation/formReservation")
    public String formAddUser(Model model,
                              RedirectAttributes redirectAttributes,
                              HttpSession session){
        model.addAttribute("allStations", stationRepository.findAll());

        return "formBooking";
    }

    @PostMapping(path ="/Responsable_reservation/resultats-disponibilites")
    public String resultatsDispo(Model model,
            @RequestParam("station_depart") Long station_depart
            ,@RequestParam("station_arrivee") Long station_arrivee
            ,@RequestParam("depart_date") String departDate
            ,@RequestParam("arrival_date") String arrivalDate
            ,@RequestParam("nb_passagers") int nb_passagers
            ,RedirectAttributes redirectAttributes){



        model.addAttribute("allStations", stationRepository.findAll());

        List<Voyage> voyages = null;
        List<Voyage> ResultVoyages = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatterForHour = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formatterForDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String now = LocalDateTime.now().format(formatter);

        String nowDate = LocalDateTime.now().format(formatterForDate);
        String dateTimeVoyage = "";
        String dateVoyage = "";

        if (!departDate.equals("") && !arrivalDate.equals("")){


        LocalDateTime arr_date = LocalDateTime.parse(arrivalDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        LocalDateTime dep_date = LocalDateTime.parse(departDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                if (voyageService.getVoyageByParam(station_depart,station_arrivee,dep_date,arr_date).isEmpty()){

                    redirectAttributes.addFlashAttribute("msg_add",false);
                    return "redirect:/Responsable_reservation/formReservation";

                }else{

                    voyages =  voyageService.getVoyageByParam(station_depart,station_arrivee,dep_date,arr_date);

            }

        }else if(!departDate.equals(""))
        {

            LocalDateTime dep_date = LocalDateTime.parse(departDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

            if (voyageService.getVoyageByParam(station_depart,station_arrivee,dep_date,null).isEmpty()){

                redirectAttributes.addFlashAttribute("msg_add",false);
                return "redirect:/Responsable_reservation/formReservation";

            }else{

            voyages =  voyageService.getVoyageByParam(station_depart,station_arrivee,dep_date,null);

            }

        }
        else if(!arrivalDate.equals("")){

            LocalDateTime arr_date = LocalDateTime.parse(arrivalDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

            if (voyageService.getVoyageByParam(station_depart,station_arrivee,null,arr_date).isEmpty()){

                redirectAttributes.addFlashAttribute("msg_add",false);
                return "redirect:/Responsable_reservation/formReservation";

            }else {

                voyages = voyageService.getVoyageByParam(station_depart, station_arrivee, null, arr_date);


            }

//          ****  if the user not chose the datetime  ****

        }else{

            if (voyageService.getVoyageByParam(station_depart,station_arrivee,null,null).isEmpty()){
                //          ****  if there is not voyages  ****
                redirectAttributes.addFlashAttribute("msg_add",false);

                return "redirect:/Responsable_reservation/formReservation";
            }else {

                voyages =  voyageService.getVoyageByParam(station_depart,station_arrivee,null,null);

                //        si le client n'est pas precis date depart et arrivee on l'affiche les train d'aujourd'hui
                for (Voyage v:voyages) {
                    dateTimeVoyage = v.getDeparature_date().format(formatter);
                    dateVoyage = v.getDeparature_date().format(formatterForDate);



                    if(dateTimeVoyage.compareTo(now)>0 && dateVoyage.compareTo(nowDate)==0){
                        ResultVoyages.add(v);
                    }


                }

                if(ResultVoyages.size() == 0){

                        redirectAttributes.addFlashAttribute("msg_add",false);
                        return "redirect:/Responsable_reservation/formReservation";

                }else{

//                    model.addAttribute("ResultVoyages",ResultVoyages);
                    redirectAttributes.addFlashAttribute("ResultVoyages",ResultVoyages);
                    redirectAttributes.addFlashAttribute("formatterForHour",formatterForHour);
                    redirectAttributes.addFlashAttribute("trainRepository",trainRepository);
                    redirectAttributes.addFlashAttribute("stationRepository",stationRepository);
                    redirectAttributes.addFlashAttribute("voyageService",voyageService);
                    redirectAttributes.addFlashAttribute("nombrePassagers",nb_passagers);


                    System.out.println("nb passager "+nb_passagers);
                    return "redirect:/Responsable_reservation/formReservation";
//                       return "voyagesDisp";
                }

            }


        }





//        Session session = entityManager.unwrap( Session.class );
//
//        Integer commentCount = session.doReturningWork(
//                connection -> {
//                    try (CallableStatement function = connection
//                            .prepareCall(
//                                    "{ ? = call fn_count_voyage(?) }" )) {
//                        function.registerOutParameter( 1, Types.INTEGER );
//                        function.setInt( 2, 1 );
//                        function.execute();
//                        return function.getInt( 1 );
//                    }
//                } );
//        System.out.println(commentCount);

        return "formBooking";
    }

//    public PriceByDistance getPriceByDistance(Long idDes,Long idOrigin) {
//        PriceByDistance priceByDistance = entityManager.createQuery("select p from PriceByDistance p", PriceByDistance.class).getSingleResult();
//        return priceByDistance;
//    }


//    public double getPriceVoyage(PriceByDistance priceByDistance, Long trainId){
//        double price = priceByDistance.getPriceDis();
//        if (trainId == 1){
//            price = price*1.2;
//        } else if (trainId == 2) {
//            price = price*1.3;
//        }else {
//            price = price*1.5;
//        }
//
//        return price;
//    }

}
