package com.project.paypass_renewal.generator;

import com.project.paypass_renewal.util.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class LinkCodeGenerator {

    public String generate(){
        return UserUtils.generateLinkCode();
    }
}
