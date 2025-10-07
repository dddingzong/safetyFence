package com.project.paypass_renewal.service;

import com.project.paypass_renewal.domain.Link;
import com.project.paypass_renewal.domain.User;
import com.project.paypass_renewal.domain.dto.request.LinkDeleteRequestDto;
import com.project.paypass_renewal.domain.dto.request.SupporterNumberRequestDto;
import com.project.paypass_renewal.domain.dto.response.LinkListResponseDto;
import com.project.paypass_renewal.domain.dto.request.LinkRequestDto;
import com.project.paypass_renewal.repository.LinkRepository;
import com.project.paypass_renewal.repository.UserAddressRepository;
import com.project.paypass_renewal.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;


    public Link saveNewLink(LinkRequestDto linkRequestDto) {

        Link link = toEntity(linkRequestDto);

        linkRepository.save(link);

        return link;
    }

    public boolean checkLinkCodeExist(LinkRequestDto linkRequestDto) {
        String linkCode = linkRequestDto.getUserLinkCode();
        return userRepository.existsByLinkCode(linkCode);
    }

    public boolean checkDuplicateLink(LinkRequestDto linkRequestDto) {
        String linkCode = linkRequestDto.getUserLinkCode();
        User user = userRepository.findByLinkCode(linkCode);

        String supporterNumber = linkRequestDto.getSupporterNumber();
        String userNumber = user.getNumber();

        return linkRepository.existsBySupporterNumberAndUserNumber(supporterNumber, userNumber);
    }

    public List<String> findUserNumbersBySupporterNumber(SupporterNumberRequestDto supporterNumberRequestDto) {
        String supporterNumber = supporterNumberRequestDtoToString(supporterNumberRequestDto);
        return linkRepository.findUserNumbersBySupporterNumber(supporterNumber);
    }

    public List<User> findUserListByNumbers (List<String> userNumbers) {
        return userRepository.findByNumberIn(userNumbers);
    }

    public List<LinkListResponseDto> userToLinkResponseDto(List<User> userList, SupporterNumberRequestDto supporterNumberRequestDto) {
        ArrayList<LinkListResponseDto> responseDtoList = new ArrayList<>();

        String supporterNumber = supporterNumberRequestDto.getSupporterNumber();

        for (User user : userList) {
            String number = user.getNumber();
            String name = user.getName();

            String homeAddress = userAddressRepository.findHomeStreetAddressByNumber(number);

            Link link = linkRepository.findByUserNumberAndSupporterNumber(number, supporterNumber);
            String relation = link.getRelation();

            LinkListResponseDto linkListResponseDto = new LinkListResponseDto(number, name, homeAddress, relation);
            responseDtoList.add(linkListResponseDto);

        }

        return responseDtoList;

    }

    @Transactional
    public int deleteLink(LinkDeleteRequestDto linkDeleteRequestDto){
        String supporterNumber = linkDeleteRequestDto.getSupporterNumber();
        String userNumber = linkDeleteRequestDto.getUserNumber();

        return linkRepository.deleteBySupporterNumberAndUserNumber(supporterNumber,userNumber);
    }

    private String supporterNumberRequestDtoToString(SupporterNumberRequestDto supporterNumberRequestDto) {
        return supporterNumberRequestDto.getSupporterNumber();
    }

    private Link toEntity(LinkRequestDto linkRequestDto) {
        String supporterNumber = linkRequestDto.getSupporterNumber();

        String linkCode = linkRequestDto.getUserLinkCode();
        User user = userRepository.findByLinkCode(linkCode);
        String userNumber = user.getNumber();

        String relation = linkRequestDto.getRelation();

        return new Link(supporterNumber, userNumber, relation);
    }


}
