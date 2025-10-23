package com.project.safetyFence.service;

import com.project.safetyFence.domain.Link;
import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.response.LinkResponseDto;
import com.project.safetyFence.repository.LinkRepository;
import com.project.safetyFence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<LinkResponseDto> getUserLink(String userNumber) {
        User user = userRepository.findByNumberWithLinks(userNumber);

        List<Link> links = user.getLinks();

        return links.stream()
                .map(link -> new LinkResponseDto(
                        link.getId(),
                        link.getUserNumber(),
                        link.getRelation()
                ))
                .toList();
    }

}
