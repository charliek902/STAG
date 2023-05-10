package edu.uob;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Edge;

/* This class parses entities within the incoming file */

public class EntitiesParserAndLoader {

    private final List<Graph> locations;
    private final List<Edge> paths;
    private final Parser parser;
    ArrayList<Graph> sections;

    public EntitiesParserAndLoader(File entitiesFile) throws IOException, ParseException, FileNotFoundException {
        this.parser = new Parser();
        FileReader fileReader = new FileReader(entitiesFile);
        parser.parse(fileReader);
        fileReader.close();
        Graph wholeDocument = parser.getGraphs().get(0);
        this.sections = wholeDocument.getSubgraphs();
        this.locations = sections.get(0).getSubgraphs();
        this.paths = sections.get(1).getEdges();
    }

    public List<Graph> getLocations() throws IOException, ParseException {
        return this.locations;
    }


    public List<Edge> getPaths() throws IOException, ParseException {
        return this.paths;
    }


}



