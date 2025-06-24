package it.unibas.common.util;

public class AnalysisContextHolder {
    private static final ThreadLocal<Boolean> modalAnalysis = ThreadLocal.withInitial(() -> false);

    public static void setModalAnalysis(boolean value) {
        modalAnalysis.set(value);
    }

    public static boolean isModalAnalysis() {
        return modalAnalysis.get();
    }
}
