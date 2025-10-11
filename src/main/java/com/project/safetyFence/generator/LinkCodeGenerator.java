package com.project.safetyFence.generator;

import com.project.safetyFence.util.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class LinkCodeGenerator {

    public String generate(){
        return UserUtils.generateLinkCode();
    }
}
