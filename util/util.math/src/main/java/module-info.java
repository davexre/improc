module util.math {
    requires transitive java.desktop;
    requires transitive slf4j.api;

    exports com.slavi.math;
    exports com.slavi.math.adjust;
    exports com.slavi.math.matrix;
    exports com.slavi.math.transform;
    exports com.slavi.util.testUtil;

}
