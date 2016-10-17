package bg.elections.pv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.Charsets;

// http://blog.bozho.net/blog/date/2016/08/03
public class PluralityVoting {
    
    private static final int CONSTITUENCY_MARGIN = 300;

    private static final int MPS = 120;
    private static final Integer DEFAULT_YEAR = 2013;
    
    private static final String DEFAULT_DATA_ROOT = "C:\\Users\\bozho\\Downloads\\cik\\export\\";
    
    private static final String PROTOCOLS_FILE = "protocols";
    private static final String PARTIES_FILE = "parties";
    private static final String VOTES_FILE = "votes";
    
    private static final Map<Integer, Map<String, String>> filenames = new HashMap<>();
    static {
        Map<String, String> elections2013 = new HashMap<>();
        elections2013.put(PROTOCOLS_FILE, "pe2013_pe_protocols.txt");
        elections2013.put(PARTIES_FILE, "pe2013_pe_cikparties.txt");
        elections2013.put(VOTES_FILE, "pe2013_pe_votes.txt");
        filenames.put(2013, elections2013);
        
        Map<String, String> elections2014 = new HashMap<>();
        elections2014.put(PROTOCOLS_FILE, "protocols_pe2014.txt");
        elections2014.put(PARTIES_FILE, "parties_pe2014.txt");
        elections2014.put(VOTES_FILE, "votes_pe2014.txt");
        filenames.put(2014, elections2014);
        
    }
    
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.withDelimiter(';');
    
    public static void main(String[] args) throws Exception {
        int year = DEFAULT_YEAR;
        String dataRoot = DEFAULT_DATA_ROOT;
        if (args.length > 0) {
            year = Integer.parseInt(args[0]);
            dataRoot = args[1];
        }
        // 2013 results have section type as first column, so we have to account for that
        boolean skipSectionType = year != 2013;
        int recordOffset = year == 2013 ? 0 : 1;
        
        CSVParser parser = CSVParser.parse(new File(dataRoot + filenames.get(year).get(PROTOCOLS_FILE)), Charsets.UTF_8, FORMAT);
        int totalVoters = 0;
        int totalEligible = 0;
        Map<String, SectionData> sectionVoters = new HashMap<>();
        for (CSVRecord record : parser) {
            if (skipSectionType || record.get(0).isEmpty() || record.get(0).equals("Д")) {
                int additionalOffset = year == 2013 ? 0 : 1; //2013 results have one extra column after 4
                SectionData data = new SectionData();
                int voters;
                if (record.get(0).equals("Д")) {
                    voters = Integer.parseInt(record.get(4));
                } else {
                    voters = Integer.parseInt(record.get(7 - recordOffset - additionalOffset));
                    data.setEligible(Integer.parseInt(record.get(4 - recordOffset)));
                }
                
                String section = record.get(1 - recordOffset);
                
                totalVoters += voters;
                totalEligible += data.getEligible();
                
                // special case for sections abroad - actual voters = eligible
                if (section.startsWith("32")) {
                    totalEligible += voters;
                    data.setEligible(voters);
                }
                sectionVoters.put(section, data);
            }
        }
        
        CSVParser partiesParser = CSVParser.parse(new File(dataRoot + filenames.get(year).get(PARTIES_FILE)), Charsets.UTF_8, FORMAT);
        
        List<String> parties = new ArrayList<>(); // idx = key
        
        for (CSVRecord record : partiesParser) {
            if (year != 2013 || record.get(0).isEmpty()) {
                parties.add(record.get(2 - recordOffset)); //offset here is because of an extra column for 2013
            }
        }
            
        CSVParser votesParser = CSVParser.parse(new File(dataRoot + filenames.get(year).get(VOTES_FILE)), Charsets.UTF_8, FORMAT);
        
        int votersPerConstituency = totalEligible / MPS;
       
        List<Integer> partyVotes = new ArrayList<>();
        resetPartyVotes(parties, partyVotes);
        
        System.out.println("Voters per constituency " + votersPerConstituency);
        System.out.println("Total voted " + totalVoters);
        List<ConstituencyResult> results = new ArrayList<>();
        List<String> constituencySections = new ArrayList<>();
        
        int currentTotalVoters = 0;
        int currentRecord = 0;
        List<CSVRecord> records = votesParser.getRecords();
        long totalRecords = records.size();
        
        int columnMultiplier = 2;
        for (CSVRecord record : records) {
            if (skipSectionType || record.get(0).isEmpty()) {
                int columnCount = (record.size() - 2 - recordOffset) / columnMultiplier - 1;
                for (int i = 0; i < columnCount; i++) {
                    int idx;
                    if (year == 2013) {
                        String idxString = record.get(2 + (i * columnMultiplier));
                        idx = Integer.parseInt(idxString) - 1; //0-based
                    } else {
                        idx = i;
                        // raw data is wrong - misses a column for some parties in some regions, which requires a shift
                        String section = record.get(0);
                        if (idx >= 20 && section.startsWith("07")) {
                            idx++;
                        }
                        if (idx >= 9 && (section.startsWith("10") || section.startsWith("17"))) {
                            idx++;
                        }
                    }
                    if (idx >= partyVotes.size()) {
                        continue;
                    }
                    // in 2013 the pairs are number:votes, and in 2014 they are valid:invalid
                    int columnIdx = 2 - recordOffset + (i * columnMultiplier) + (year == 2013 ? 1 : 0);
                    
                    String votesString = record.get(columnIdx);
                    if (votesString.isEmpty()) {
                        System.out.println("Empty string for columnIdx " + columnIdx + " and row " + currentRecord);
                        continue;
                    }
                    int votes = Integer.parseInt(votesString);
                    partyVotes.set(idx, partyVotes.get(idx) + votes);
                }
                
                String section = record.get(1 - recordOffset);
                if (sectionVoters.containsKey(section)) {
                    currentTotalVoters += sectionVoters.get(section).getEligible();
                    constituencySections.add(section);
                    // the last constituency can be bigger in order to get the exact amount of MPs
                    if (currentTotalVoters > votersPerConstituency - CONSTITUENCY_MARGIN && results.size() < MPS - 1) {
                        ConstituencyResult result = getResult(partyVotes, parties);
                        result.setSections(new ArrayList<>(constituencySections));
                        results.add(result);
                        constituencySections.clear();
                        currentTotalVoters = 0;
                        resetPartyVotes(parties, partyVotes);
                    }
                } else {
                    System.out.println("Missing section: " + section);                    
                }
            }
            if (currentRecord == totalRecords - 1) { //the final MP
                ConstituencyResult result = getResult(partyVotes, parties); 
                result.setSections(new ArrayList<>(constituencySections));
                results.add(result);
            }
            currentRecord ++;
        }
        
        results.forEach(r -> System.out.println(r + " : " + r.getSections()));
        Map<String, List<ConstituencyResult>> totals = results.stream().collect(Collectors.groupingBy(ConstituencyResult::getWinnerParty));
        totals.forEach((k, v) -> {
            int firstRoundWins = v.stream().filter(w -> w.isFirstRoundWin()).collect(Collectors.toList()).size();
            System.out.println(k + " = " + v.size() + " (first round: " + firstRoundWins + ")");
        });
    }

    private static void resetPartyVotes(List<String> parties, List<Integer> partyVotes) {
        partyVotes.clear();
        for (int i = 0; i < parties.size(); i++) {
            partyVotes.add(0);
        }
    }
    
    private static ConstituencyResult getResult(List<Integer> partyVotes, List<String> parties) {
        int maxVotes = Collections.max(partyVotes);
        //assuming random winner if more than 1 party with equal votes
        int idx = partyVotes.indexOf(maxVotes);
        ConstituencyResult winner = new ConstituencyResult();
        winner.setWinnerParty(parties.get(idx));
        winner.setResult(maxVotes);
        List<Integer> runnerUpVotes = new ArrayList<>(partyVotes);
        runnerUpVotes.remove((Object) maxVotes);
        int maxRunnerUpVotes = Collections.max(runnerUpVotes);
        int runnerUpIdx = partyVotes.indexOf(maxRunnerUpVotes);
        
        winner.setRunnerUpParty(parties.get(runnerUpIdx));
        winner.setRunnerUpResult(maxRunnerUpVotes);
        int remainingVotes = runnerUpVotes.stream().mapToInt(i -> i).sum();
        if (maxVotes > remainingVotes) {
            winner.setFirstRoundWin(true);
        }
        
        return winner;
    }

    public static final class SectionData {
        private int eligible;
        private int activity;

        public int getEligible() {
            return eligible;
        }
        public void setEligible(int eligible) {
            this.eligible = eligible;
        }
        public int getActivity() {
            return activity;
        }
        public void setActivity(int activity) {
            this.activity = activity;
        }
    }
    
    public static final class ConstituencyResult {
        private String winnerParty;
        private int result;
        private String runnerUpParty;
        private int runnerUpResult;
        private List<String> sections;
        private boolean firstRoundWin;
        
        public String getWinnerParty() {
            return winnerParty;
        }
        public void setWinnerParty(String name) {
            this.winnerParty = name;
        }
        public int getResult() {
            return result;
        }
        public void setResult(int result) {
            this.result = result;
        }
        public String getRunnerUpParty() {
            return runnerUpParty;
        }
        public void setRunnerUpParty(String runnerUpName) {
            this.runnerUpParty = runnerUpName;
        }
        public int getRunnerUpResult() {
            return runnerUpResult;
        }
        public void setRunnerUpResult(int runnerUpResult) {
            this.runnerUpResult = runnerUpResult;
        }
        
        public List<String> getSections() {
            return sections;
        }
        public void setSections(List<String> sections) {
            this.sections = sections;
        }
        public boolean isFirstRoundWin() {
            return firstRoundWin;
        }
        public void setFirstRoundWin(boolean firstRoundWin) {
            this.firstRoundWin = firstRoundWin;
        }
        @Override
        public String toString() {
            return "ConstituencyResult [winnerParty=" + winnerParty + ", result=" + result
                    + ", runnerUpParty=" + runnerUpParty + ", runnerUpResult=" + runnerUpResult + "]";
        }
    }
}
