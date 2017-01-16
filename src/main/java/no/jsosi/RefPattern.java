package no.jsosi;

class RefPattern {

    private static final String[] allCandidates;

    static {
        allCandidates = new String[2];
        allCandidates[0] = "." + GeometryType.KURVE + " ";
        allCandidates[1] = "." + GeometryType.BUEP + " ";
    }

    private final String[] restCandidates;

    public RefPattern() {
        restCandidates = new String[allCandidates.length];
        reset();
    }

    public boolean match(char c, int p) {
        int rest = 0;
        for (int i = 0; i < restCandidates.length; i++) {
            String candidate = restCandidates[i];
            if (candidate == null) {
                continue;
            }
            if (candidate.length() < p || candidate.charAt(p) != c) {
                restCandidates[i] = null;
                continue;
            }
            rest++;
        }
        return rest > 0;
    }

    public void reset() {
        System.arraycopy(allCandidates, 0, restCandidates, 0, allCandidates.length);
    }

    public boolean atEndOfMatch(int p) {
        int ends = 0;
        for (int i = 0; i < restCandidates.length; i++) {
            String restCandidate = restCandidates[i];
            if (restCandidate == null) {
                continue;
            }
            if (restCandidate != null && restCandidate.length() == (p + 1)) {
                ends++;
            }
        }
        return ends == 1;
    }

}