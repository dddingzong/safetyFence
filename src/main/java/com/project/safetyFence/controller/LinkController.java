package com.project.safetyFence.controller;

import com.project.safetyFence.domain.User;
import com.project.safetyFence.domain.dto.request.LinkDeleteRequestDto;
import com.project.safetyFence.domain.dto.request.LinkRequestDto;
import com.project.safetyFence.domain.dto.request.SupporterNumberRequestDto;
import com.project.safetyFence.domain.dto.response.LinkListResponseDto;
import com.project.safetyFence.exception.CustomException;
import com.project.safetyFence.exception.ErrorResult;
import com.project.safetyFence.service.LinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/link/saveNewLink")
    public ResponseEntity<String> saveNewLink(@RequestBody LinkRequestDto linkRequestDto) {

        if (!linkService.checkLinkCodeExist(linkRequestDto)) {
            log.info("링크코드가 존재하지 않습니다.");
            throw new CustomException(ErrorResult.LINK_CODE_NOT_EXIST);
        }

        if (linkService.checkDuplicateLink(linkRequestDto)){
            log.info("중복된 사용자가 존재합니다.");
            throw new CustomException(ErrorResult.LINK_USER_AND_SUPPORTER_DUPLICATE);
        }

        linkService.saveNewLink(linkRequestDto);
        return ResponseEntity.ok("saveSuccess");
    }

    @PostMapping("/link/getLinkList")
    public ResponseEntity<List<LinkListResponseDto>> getLinkList(@RequestBody SupporterNumberRequestDto supporterNumberRequestDto) {

        log.info("이용자 조회를 시작합니다.");

        List<String> userNumbers = linkService.findUserNumbersBySupporterNumber(supporterNumberRequestDto);

        log.info("이용자 번호 조회 완료: {}", userNumbers);

        List<User> userList = linkService.findUserListByNumbers(userNumbers);

        List<LinkListResponseDto> linkListResponseDtoList = linkService.userToLinkResponseDto(userList, supporterNumberRequestDto);

        return ResponseEntity.ok(linkListResponseDtoList);
    }

    @PostMapping("/link/deleteLink")
    public ResponseEntity<String> deleteLink(@RequestBody LinkDeleteRequestDto linkDeleteRequestDto) {

        log.info("링크 삭제 요청을 받았습니다.");

        int deleteCount = linkService.deleteLink(linkDeleteRequestDto);

        return ResponseEntity.ok("deleteSuccess. 삭제된 데이터 개수: " + deleteCount);
    }

}
