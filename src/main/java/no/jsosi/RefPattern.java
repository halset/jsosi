package no.jsosi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class RefPattern {

    private static final List<String> allCandidates = Arrays.asList("." + GeometryType.KURVE + " ",
            "." + GeometryType.BUEP + " ");

    private final List<String> restCandidates = new ArrayList<>(allCandidates);

    public RefPattern() {
    }

    public boolean match(char c, int p) {
        for (Iterator<String> it = restCandidates.iterator(); it.hasNext();) {
            String candidate = it.next();
            if (candidate.length() < p) {
                it.remove();
                continue;
            }
            if (candidate.charAt(p) != c) {
                it.remove();
                continue;
            }
        }
        return !restCandidates.isEmpty();
    }

    public void reset() {
        restCandidates.clear();
        restCandidates.addAll(allCandidates);
    }

    public boolean atEndOfMatch(int p) {
        return restCandidates.size() == 1 && restCandidates.get(0).length() == (p + 1);
    }

}