package org.icpclive.events.codeforces.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icpclive.events.codeforces.api.data.CFSubmission;
import org.icpclive.events.codeforces.api.results.CFStandings;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author egor@egork.net
 */
public class CFApiCentral {
    private static final Logger log = LogManager.getLogger(CFApiCentral.class);
    private static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public final int contestId;

    public CFApiCentral(int contestId) {
        this.contestId = contestId;
    }

    public CFStandings getStandings() {
        try {
            JsonNode node = apiRequest("contest.standings?contestId=" + contestId);
            return mapper.treeToValue(node, CFStandings.class);
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }

    public List<CFSubmission> getStatus() {
        try {
            JsonNode node = apiRequest("contest.status?contestId=" + contestId);
            List<CFSubmission> result = new ArrayList<>();
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                result.add(mapper.treeToValue(elements.next(), CFSubmission.class));
            }
            return result;
        } catch (IOException e) {
            log.error(e);
            return null;
        }
    }

    private static JsonNode apiRequest(String request) throws IOException {
        String address = "https://codeforces.com/api/" + request;
        URL url = new URL(address);
        JsonNode node = mapper.readTree(url.openConnection().getInputStream());
        if (node == null || !"OK".equals(node.get("status").asText())) {
            throw new IOException("Request " + request + " unsuccessful");
        }
        node = node.get("result");
        return node;
    }
}
