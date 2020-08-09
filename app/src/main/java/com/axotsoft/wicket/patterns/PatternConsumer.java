package com.axotsoft.wicket.patterns;

import com.axotsoft.wicket.patterns.records.PatternRecord;

public interface PatternConsumer {
    void accept(PatternRecord command);
}
