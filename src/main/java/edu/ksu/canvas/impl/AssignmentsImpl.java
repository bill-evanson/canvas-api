package edu.ksu.canvas.impl;

import com.google.common.collect.ImmutableMap;
import edu.ksu.canvas.enums.AssignmentType;
import edu.ksu.canvas.exception.InvalidOauthTokenException;
import edu.ksu.canvas.interfaces.AssignmentReader;
import edu.ksu.canvas.interfaces.AssignmentWriter;
import edu.ksu.canvas.model.Assignment;
import edu.ksu.canvas.model.Delete;
import edu.ksu.canvas.net.Response;
import edu.ksu.canvas.net.RestClient;
import edu.ksu.canvas.util.CanvasURLBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AssignmentsImpl extends BaseImpl implements AssignmentReader, AssignmentWriter{
    private static final Logger LOG = Logger.getLogger(AssignmentReader.class);

    public AssignmentsImpl(String canvasBaseUrl, Integer apiVersion, String oauthToken, RestClient restClient) {
        super(canvasBaseUrl, apiVersion, oauthToken, restClient);
    }

    @Override
    public Optional<Assignment> getSingleCourse(String oauthToken, String courseId, String assignmentId) throws IOException {
        return null;
    }

    @Override
    public Optional<Assignment> createAssignment(String oauthToken, String courseId, String assignmentName, String pointsPossible) throws InvalidOauthTokenException, IOException {
        return createAssignment(oauthToken, courseId, assignmentName, pointsPossible,
                AssignmentType.ON_PAPER, true, true);
    }

    @Override
    public Optional<Assignment> createAssignment(String oauthToken, String courseId, String assignmentName, String pointsPossible,
                                                 AssignmentType assignmentType, boolean published, boolean muted) throws InvalidOauthTokenException, IOException {
        ImmutableMap<String, List<String>> parameters = ImmutableMap.<String,List<String>>builder()
                .put("assignment[name]", Collections.singletonList(assignmentName))
                .put("assignment[submission_types]", Collections.singletonList(assignmentType.toString()))
                .put("assignment[points_possible]", Collections.singletonList(pointsPossible))
                .put("assignment[published]", Collections.singletonList(String.valueOf(published)))
                .put("assignment[muted]", Collections.singletonList(String.valueOf(muted))).build();
        String url = CanvasURLBuilder.buildCanvasUrl(canvasBaseUrl, apiVersion,
                "courses/" + courseId + "/assignments", parameters);
        Response response = canvasMessenger.sendToCanvas(oauthToken, url, Collections.emptyMap());
        if (response.getErrorHappened() || response.getResponseCode() != 200) {
            LOG.error("Errors creating assignment for course " + courseId + " with assignmentName " + assignmentName);
            return Optional.empty();
        }
        return responseParser.parseToObject(Assignment.class, response);
    }

    @Override
    public Boolean deleteAssignment(String oauthToken, String courseId, String assignmentId) throws InvalidOauthTokenException, IOException {
        Map<String, String> postParams = new HashMap<>();
        postParams.put("event", "delete");
        String createdUrl = CanvasURLBuilder.buildCanvasUrl(canvasBaseUrl, apiVersion,
                "courses/" + courseId + "/assignments/" + assignmentId, Collections.emptyMap());
        Response response = canvasMessenger.deleteFromCanvas(oauthToken, createdUrl, postParams);
        LOG.debug("response " + response.toString());
        if(response.getErrorHappened() || response.getResponseCode() != 200){
            LOG.debug("Failed to delete assignment, error message: " + response.toString());
            return false;
        }
        Optional<Delete> responseParsed = responseParser.parseToObject(Delete.class, response);
        return responseParsed.get().getDelete();
    }

}