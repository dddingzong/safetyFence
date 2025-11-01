package com.project.safetyFence.link.generator;

import com.project.safetyFence.user.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class LinkCodeGenerator {

    public String generate(){
        return UserUtils.generateLinkCode();
    }
}
