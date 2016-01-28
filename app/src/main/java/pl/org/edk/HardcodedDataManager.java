package pl.org.edk;

import pl.org.edk.Database.DbManager;
import pl.org.edk.Database.Entities.Area;
import pl.org.edk.Database.Entities.Route;
import pl.org.edk.Database.Entities.Territory;

/**
 * Created by Admin on 2016-01-28.
 */
public final class HardcodedDataManager {
    public static void CreateTerritoriesAndAreas(){
        Territory territory1 = new Territory("dolnośląskie");
        territory1.getAreas().add(new Area("Wrocław"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory1);

        Territory territory2 = new Territory("kujawsko-pomorskie");
        territory2.getAreas().add(new Area("Bydgoszcz"));
        territory2.getAreas().add(new Area("Toruń"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory2);

        Territory territory3 = new Territory("lubelskie");
        territory3.getAreas().add(new Area("Lublin"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory3);

        Territory territory4 = new Territory("lubuskie");
        territory4.getAreas().add(new Area("Gorzów Wielkopolski"));
        territory4.getAreas().add(new Area("Zielona Góra"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory4);

        Territory territory5 = new Territory("łódzkie");
        territory5.getAreas().add(new Area("Lódź"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory5);

        Territory territory6 = new Territory("małopolskie");
        territory6.getAreas().add(new Area("Kraków"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory6);

        Territory territory7 = new Territory("mazowieckie");
        territory7.getAreas().add(new Area("Warszwawa"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory7);

        Territory territory8 = new Territory("opolskie");
        territory8.getAreas().add(new Area("Opole"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory8);

        Territory territory9 = new Territory("podkarpackie");
        territory9.getAreas().add(new Area("Rzeszów"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory9);

        Territory territory10 = new Territory("podlaskie");
        territory10.getAreas().add(new Area("Białystok"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory10);

        Territory territory11 = new Territory("Gdańsk");
        territory11.getAreas().add(new Area("Wrocław"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory11);

        Territory territory12 = new Territory("śląskie");
        territory12.getAreas().add(new Area("Katowice"));
        territory12.getAreas().add(new Area("Gliwice"));
        territory12.getAreas().add(new Area("Bytom"));
        territory12.getAreas().add(new Area("Zabrze"));
        territory12.getAreas().add(new Area("Ruda Sląska"));
        territory12.getAreas().add(new Area("Chorzów"));
        territory12.getAreas().add(new Area("Knurów"));
        territory12.getAreas().add(new Area("Wodzisław Slaski"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory12);

        Territory territory13 = new Territory("świętokrzyskie");
        territory13.getAreas().add(new Area("Kielce"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory13);

        Territory territory14 = new Territory("warmińsko-mazurskie");
        territory14.getAreas().add(new Area("Olsztyn"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory14);

        Territory territory15 = new Territory("wielkopolskie");
        territory15.getAreas().add(new Area("Poznań"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory15);

        Territory territory16 = new Territory("zachodniopomorskie");
        territory16.getAreas().add(new Area("Szczecin"));
        DbManager.getInstance().getTerritoryService().InsertTerritoryWithAreas(territory16);
    }

    public static void CreateRoutes() {
        // dolnośląskie
        Route route1 = new Route(1, "2016-01-29 00:02", "Wrocław Główny");
        // TODO: insert stations for the Route here
        //route1.getStations().add()
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route1);

        // kujawsko-pomorskie
        Route route2 = new Route(3, "2016-01-29 00:02", "Toruń Główny");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route2);

        // mazowieckie
        Route route3 = new Route(9, "2016-01-29 00:02", "Warszawa Centralna");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route3);
        Route route4 = new Route(9, "2016-01-29 00:02", "Warszawa Wschodnia");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route4);
        Route route5 = new Route(9, "2016-01-29 00:02", "Warszawa Zachodnia");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route5);

        // śląskie
        Route route6 = new Route(15, "2016-01-29 00:02", "Gliwice");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route6);
        Route route7 = new Route(15, "2016-01-29 00:02", "Gliwice Labedy");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route7);
        Route route8 = new Route(15, "2016-01-29 00:02", "Gliwice Sikornik");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route8);
        Route route9 = new Route(18, "2016-01-29 00:02", "Halemba");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route9);
        Route route10 = new Route(14, "2016-01-29 00:02", "Katowice - Spodek");
        DbManager.getInstance().getRouteService().InsertRouteWithStations(route10);
    }
}
