package net.egork.teaminfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.egork.teaminfo.data.University;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author egor@egork.net
 */
public class Utils {
    public static ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static Log log = LogFactory.getLog(Utils.class);

    public static String loadPage(String address) throws Exception {
        URL url = new URL(address);
        URLConnection connection = url.openConnection();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return getString(reader);
        } catch (IOException e) {
            log.error("Can't load page " + address);
            return loadPage(address);
        }
    }

    private static String getString(BufferedReader reader) throws IOException {
        String s;
        StringBuilder result = new StringBuilder();
        while ((s = reader.readLine()) != null) {
            result.append(s).append("\n");
        }
        return result.toString();
    }

    public static String readPage(String fileName) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        return getString(reader);
    }

    public static String appropriateEnd(int place) {
        if (place % 100 >= 10 && place % 100 < 20) {
            return "th";
        }
        int last = place % 10;
        if (last == 1) {
            return "st";
        }
        if (last == 2) {
            return "nd";
        }
        if (last == 3) {
            return "rd";
        }
        return "th";
    }

    public static boolean sameUniversity(University fromRecord, University fromSnark, Map<String, String> map) {
        if (Objects.equals(fromRecord.getFullName(), fromSnark.getFullName())) {
            return true;
        }
        if (Objects.equals(fromRecord.getShortName(), fromSnark.getFullName())) {
            return true;
        }
        if (fromSnark.getFullName() == null) {
            return false;
        }
        if (fromRecord.getShortName().startsWith("U ") && fromSnark.getFullName().startsWith("U of ") &&
                fromRecord.getShortName().substring(2).equals(fromSnark.getFullName().substring(5)))
        {
            return true;
        }
        if (fromSnark.getFullName().startsWith("U of ") &&
                fromRecord.getShortName().equals(fromSnark.getFullName().substring(5)))
        {
            return true;
        }
        if (fromSnark.getFullName().contains(fromRecord.getShortName())) {
            return true;
        }
        if (fromSnark.getFullName().equals(map.get(fromRecord.getFullName()))) {
            return true;
        }
        return false;
    }

    public static JsonNode codeforcesApiRequest(String apiRequest) throws IOException {
        String address = "http://codeforces.com/api/" + apiRequest;
        URL url = new URL(address);
        JsonNode node = mapper.readTree(url.openConnection().getInputStream());
        if (node == null) {
            log.error("Request " + apiRequest + " unsuccessful");
            return null;
        }
        if (!node.get("status").asText().equals("OK")) {
            log.error("Request " + apiRequest + " unsuccessful");
            return null;
        }
        node = node.get("result");
        return node;
    }

    public static boolean compatible(String o1, String o2) {
        return o1 == null || o2 == null || o1.equals(o2);
    }

    public static <T>List<T> readList(String filename, Class<T> aClass) throws Exception {
        JsonNode node = mapper.readTree(new File(filename));
        Iterator<JsonNode> elements = node.elements();
        List<T> list = new ArrayList<>();
        while (elements.hasNext()) {
            list.add(mapper.treeToValue(elements.next(), aClass));
        }
        return list;
    }

    public static String getYears(List<String> years) {
        StringBuilder result = new StringBuilder();
        int start = -1;
        int end = -1;
        boolean first = true;
        for (String year : years) {
            int y = Integer.parseInt(year);
            if (end + 1 != y) {
                if (start != -1) {
                    if (first) {
                        first = false;
                    } else {
                        result.append(", ");
                    }
                    if (end - start > 1) {
                        result.append(start).append("-").append(end);
                    } else if (end - start == 1) {
                        result.append(start).append(", ").append(end);
                    } else {
                        result.append(start);
                    }
                }
                start = y;
            }
            end = y;
        }
        if (first) {
            first = false;
        } else {
            result.append(", ");
        }
        if (end - start > 1) {
            result.append(start).append("-").append(end);
        } else if (end - start == 1) {
            result.append(start).append(", ").append(end);
        } else {
            result.append(start);
        }
        return result.toString();
    }

    private static String UMLAUTS = "ŠŒŽšœžŸ¥µÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝàáâãäåæçèéêëìíîïðñòóôõöøùúûüýÿ";
    private static String WITHOUT = "SOZsozYYuAAAAAAACEEEEIIIIDNOOOOOOUUUUYaaaaaaaceeeeiiiionoooooouuuuyy";

    public static String replaceUmlauts(String name) {
        name = name.replace("ß", "ss");
        for (int i = 0; i < UMLAUTS.length(); i++) {
            name = name.replace(UMLAUTS.charAt(i), WITHOUT.charAt(i));
        }
        return name;
    }
}
