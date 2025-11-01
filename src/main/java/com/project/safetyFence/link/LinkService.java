package com.project.safetyFence.link;

import com.project.safetyFence.link.domain.Link;
import com.project.safetyFence.user.domain.User;
import com.project.safetyFence.link.dto.LinkRequestDto;
import com.project.safetyFence.link.dto.LinkResponseDto;
import com.project.safetyFence.common.exception.CustomException;
import com.project.safetyFence.common.exception.ErrorResult;
import com.project.safetyFence.link.LinkRepository;
import com.project.safetyFence.user.UserRepository;
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

    @Transactional
    public void addLinkUser(String userNumber, LinkRequestDto linkRequestDto) {
        User user = userRepository.findByNumber(userNumber);
        String linkCode = linkRequestDto.getLinkCode();

        // 1. targetUser null 체크
        User targetUser = userRepository.findByLinkCode(linkCode);
        if (targetUser == null) {
            throw new CustomException(ErrorResult.LINK_CODE_NOT_EXIST);
        }

        // 2. 자기 자신 추가 방지
        if (user.getNumber().equals(targetUser.getNumber())) {
            throw new CustomException(ErrorResult.CANNOT_ADD_SELF_AS_LINK);
        }

        // 3. 중복 추가 방지 (이미 링크에 존재하는지 확인)
        boolean alreadyExists = user.getLinks().stream()
                .anyMatch(link -> link.getUserNumber().equals(targetUser.getNumber()));

        if (alreadyExists) {
            throw new CustomException(ErrorResult.LINK_ALREADY_EXISTS);
        }

        // 4. 링크 추가
        Link link = new Link(user, targetUser.getNumber(), linkRequestDto.getRelation());
        user.addLink(link);

        userRepository.save(user);
    }

    public void deleteLinkUser(String userNumber, String linkUserNumber) {
        User user = userRepository.findByNumberWithLinks(userNumber);

        Link linkToDelete = user.getLinks().stream()
                .filter(link -> link.getUserNumber().equals(linkUserNumber))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorResult.LINK_NOT_FOUND));

        user.removeLink(linkToDelete);

    }

    public boolean hasLink(String subscriberNumber, String targetUserNumber) {
        return linkRepository.existsByUser_NumberAndUserNumber(subscriberNumber, targetUserNumber);
    }

}
