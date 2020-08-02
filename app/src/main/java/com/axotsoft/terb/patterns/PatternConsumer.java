package com.axotsoft.terb.patterns;

import com.axotsoft.terb.patterns.records.PatternRecord;

public interface PatternConsumer {
    void accept(PatternRecord command);
}
