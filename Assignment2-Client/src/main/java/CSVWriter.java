import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVWriter {

    private PrintWriter writer;
    private StringBuilder csvContent;
    List<long[]> timeList;
    List<Integer> postList;
    List<Integer> getList;

    public CSVWriter(PrintWriter writer, StringBuilder csvContent) {
        this.writer = writer;
        this.csvContent = csvContent;
    }

    public CSVWriter(StringBuilder csvContent) throws FileNotFoundException {
        this.writer = new PrintWriter(new File("records.csv"));
        this.csvContent = csvContent;
        this.postList = Collections.synchronizedList(new ArrayList<Integer>());
        this.getList = Collections.synchronizedList(new ArrayList<Integer>());
        this.timeList = Collections.synchronizedList(new ArrayList<long[]>());
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public StringBuilder getCsvContent() {
        return csvContent;
    }

    public void setCsvContent(StringBuilder csvContent) {
        this.csvContent = csvContent;
    }

    public synchronized void writeRecord(long startTime, String requestType, long latency, int responseCode) {
        csvContent.append(startTime);
        csvContent.append(',');
        csvContent.append(requestType);
        csvContent.append(',');
        csvContent.append(latency);
        csvContent.append(',');
        csvContent.append(responseCode);
        csvContent.append('\n');
        if (requestType.equals("POST")) {
            postList.add((int) latency);
        }
        else {
            getList.add((int) latency);
        }
    }

    public void exportFile() {
        writer.write(csvContent.toString());
    }

    public List<Integer> getPostList() {
        return postList;
    }

    public List<long[]> getTimeList() {
        return timeList;
    }

    public void setPostList(List<Integer> postList) {
        this.postList = postList;
    }

    public List<Integer> getGetList() {
        return getList;
    }

    public void setGetList(List<Integer> getList) {
        this.getList = getList;
    }
}
