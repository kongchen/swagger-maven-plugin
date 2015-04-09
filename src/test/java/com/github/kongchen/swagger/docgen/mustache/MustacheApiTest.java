package com.github.kongchen.swagger.docgen.mustache;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.Operation;
import org.testng.annotations.Test;
import scala.Option;
import scala.collection.immutable.Map;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by chekong on 15/4/8.
 */
public class MustacheApiTest {

    MustacheDocument doc = new MustacheDocument(new ApiListing("", "", "", "",
            Utils.toScalaImmutableList(new ArrayList<String>()),
            Utils.toScalaImmutableList(new ArrayList<String>()),
            Utils.toScalaImmutableList(new ArrayList<String>()),
            Utils.toScalaImmutableList(new ArrayList<Authorization>()),
            Utils.toScalaImmutableList(new ArrayList<ApiDescription>()), Option.<Map<String,Model>>empty(),Option.<String>empty(),0));

    @Test
    public void testResetOperationPositions() throws Exception {
        MustacheApi api = new MustacheApi("", new ApiDescription("", Option.apply(""), null, false));
        api.addOperation(new MustacheOperation(doc, new Operation("", "1", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "2", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "3", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.resetOperationPositions();
        assertEquals(1, api.getOperations().get(0).getOpIndex());
        assertEquals("1", api.getOperations().get(0).getSummary());

        assertEquals(2, api.getOperations().get(1).getOpIndex());
        assertEquals("2", api.getOperations().get(1).getSummary());

        assertEquals(3, api.getOperations().get(2).getOpIndex());
        assertEquals("3", api.getOperations().get(2).getSummary());
    }

    @Test
    public void testResetOpOrder2() {
        MustacheApi api = new MustacheApi("", new ApiDescription("", Option.apply(""), null, false));
        api.addOperation(new MustacheOperation(doc, new Operation("", "1", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "2", "", "", "", 2, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "3", "", "", "", 3, null, null, null, null, null, null, Option.<String>empty())));
        api.resetOperationPositions();
        assertEquals(1, api.getOperations().get(0).getOpIndex());
        assertEquals("1", api.getOperations().get(0).getSummary());

        assertEquals(2, api.getOperations().get(1).getOpIndex());
        assertEquals("2", api.getOperations().get(1).getSummary());

        assertEquals(3, api.getOperations().get(2).getOpIndex());
        assertEquals("3", api.getOperations().get(2).getSummary());
    }

    @Test
    public void testResetOpOrder3() {
        MustacheApi api = new MustacheApi("", new ApiDescription("", Option.apply(""), null, false));
        api.addOperation(new MustacheOperation(doc, new Operation("", "1", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "2", "", "", "", 0, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "3", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.resetOperationPositions();
        assertEquals(1, api.getOperations().get(0).getOpIndex());
        assertEquals("2", api.getOperations().get(0).getSummary());

        assertEquals(2, api.getOperations().get(1).getOpIndex());
        assertEquals("1", api.getOperations().get(1).getSummary());

        assertEquals(3, api.getOperations().get(2).getOpIndex());
        assertEquals("3", api.getOperations().get(2).getSummary());
    }

    @Test
    public void testResetOpOrder4() {
        MustacheApi api = new MustacheApi("", new ApiDescription("", Option.apply(""), null, false));
        api.addOperation(new MustacheOperation(doc, new Operation("", "1", "", "", "", 2, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "2", "", "", "", 1, null, null, null, null, null, null, Option.<String>empty())));
        api.addOperation(new MustacheOperation(doc, new Operation("", "3", "", "", "", 4, null, null, null, null, null, null, Option.<String>empty())));
        api.resetOperationPositions();
        assertEquals(1, api.getOperations().get(0).getOpIndex());
        assertEquals("2", api.getOperations().get(0).getSummary());

        assertEquals(2, api.getOperations().get(1).getOpIndex());
        assertEquals("1", api.getOperations().get(1).getSummary());

        assertEquals(3, api.getOperations().get(2).getOpIndex());
        assertEquals("3", api.getOperations().get(2).getSummary());
    }
}