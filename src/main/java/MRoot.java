import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class MRoot {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); //не получается, узнать.
    private static String path = "https://skillbox-java.github.io/";
    private static Map<String, Line> lines = new LinkedHashMap<>();
    private static Map<Line, List<Station>> stations = new LinkedHashMap<>();
    private static Map<String, List<String>> connections = new LinkedHashMap<>();

    public void JsonParse() throws IOException {

        Document doc = Jsoup.connect(path).maxBodySize(0).get();
        Elements eLines = doc.getElementsByClass("js-metro-line"); //поиск элементов в коде с параметром class
        addLines(eLines);
        Elements eStations = doc.getElementsByClass("js-metro-stations");
        addStations(eStations);
        addConnections(eStations);

//        System.out.println(eStations.get(0).child(0).child(2).className()); //линии переходы
//        System.out.println(eStations.get(0).child(5).child(3).attr("title")); //название перехода
//        System.out.println(eStations.get(0).child(0).text()); //название станции
//        System.out.println(eStations.get(0).attr("data-line")); //номер линии
    }

    public void addLines(Elements elements) {
        for (Element e : elements) {
            Line line = new Line(e.text(), e.attr("data-line")); // e.text названия, attr номера
            lines.put(line.getNumber(), line); //line названия линий, getNumber номера
        }
    }

    public void addStations(Elements elements) {
        for (Element e : elements) {
            String name = lines.get(e.attr("data-line")).getName();
            String number = lines.get(e.attr("data-line")).getNumber();
            Line line = new Line(name, number);
            String[] st = e.text().trim().split("\\d+.");
            List<Station> stList = new ArrayList<>();
            for (int i = 1; i < st.length; i++) {
                Station s = new Station(st[i].trim(), line);
                stList.add(s);
            }
            stations.put(line, stList);
        }
    }

    public void addConnections(Elements elements) {
        for (int i = 0; i < elements.size(); i++) {
            String number = elements.get(i).attr("data-line");
            for (int j = 0; j < elements.get(i).childrenSize(); j++) {
                String st = elements.get(i).child(j).text().replaceAll("\\d+.", "").trim();
                List<String> connection = new ArrayList<>();
                List<String> con = new ArrayList<>();
                for (int k = 2; k < elements.get(i).child(j).childrenSize(); k++) {
                    String title = elements.get(i).child(j).child(k).attr("title");
                    String conNumber = elements.get(i).child(j).child(k).className().substring(18);
                    connection.add(title);
                    con.add(title + " Line: " + conNumber);
                }
                if (connection.size() != 0) {
                    connections.put(st + " L: " + number, con);
                }
            }
        }
    }


    public void createJson() throws IOException {
        FileWriter file = new FileWriter("lib/Metro.json");
        JSONObject jsonObject = new JSONObject();
        JSONArray linesJsonArray = new JSONArray();
        JSONObject stationsJsonObject = new JSONObject();
        JSONObject connectionsJsonObject = new JSONObject();

        Set<Line> tempS = stations.keySet();
        for (Line line : tempS) { //название линий
            JSONArray temp = new JSONArray();
            List<Station> stationList = stations.get(line); //вытаскиваем станции(массивы) по имени линии
            for (int i = 0; i < stationList.size(); i++) {
                String station = stationList.get(i).getName(); //имя станции по номерy в массиве
                temp.add(station); //добавляем в виде массива JSON

            }
            stationsJsonObject.put(line, temp);
        }

        Set<String> tempL = lines.keySet(); //номера линий
        for (String num : tempL) {
            JSONObject temp = new JSONObject();
            String line = lines.get(num).getName();
            temp.put("Number", num);
            temp.put("Line", line);
            linesJsonArray.add(temp);
        }

        Set<String> tempC = connections.keySet();
        for (String s : tempC) {
            JSONArray temp = new JSONArray();
            List<String> conList = connections.get(s);
            for (int i = 0; i < conList.size(); i++) {
                String con = conList.get(i);
                temp.add(con);
            }
            connectionsJsonObject.put(s, temp);
        }

        jsonObject.put("Stations", stationsJsonObject);
        jsonObject.put("Lines", linesJsonArray);
        jsonObject.put("Connections",connectionsJsonObject);
        file.write(jsonObject.toJSONString());
        file.close();
    }


    public static String parseFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<String> lines = Files.readAllLines(Path.of(path));
        lines.forEach(line -> sb.append(line));
        return sb.toString();
    }

    public static void JsonParser() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(parseFile("H:\\MoskovMetro\\lib\\Metro.json"));
        Map<String, List<String>> stations = new TreeMap<>((Map<String, List<String>>) jsonObject.get("Stations")); //приводим к TreeMap, чтобы вывести в алфавитном порядке(зачем? чтобы было красиво)
        System.out.println(stations.keySet());
        for (String lineName : stations.keySet()) {
            ArrayList<String> sArray = (ArrayList<String>) stations.get(lineName);
            Collections.sort(sArray);
                System.out.println(lineName + "\n" + " Количество станций - " + sArray.size());
        }
    }


}