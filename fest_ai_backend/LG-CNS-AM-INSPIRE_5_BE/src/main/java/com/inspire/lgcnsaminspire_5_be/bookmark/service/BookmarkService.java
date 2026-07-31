package com.inspire.lgcnsaminspire_5_be.bookmark.service;

import com.inspire.lgcnsaminspire_5_be.auth.repository.UserRepository;
import com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto.BookmarkRequestDTO;
import com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto.BookmarkResponseDTO;
import com.inspire.lgcnsaminspire_5_be.bookmark.domain.dto.BookmarkUpdateRequestDTO;
import com.inspire.lgcnsaminspire_5_be.bookmark.domain.entity.BookmarkEntity;
import com.inspire.lgcnsaminspire_5_be.bookmark.repository.BookmarkRepository;
import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import com.inspire.lgcnsaminspire_5_be.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    public void createBookmark(Long userId, BookmarkRequestDTO dto) {
        FestivalEntity festival = festivalRepository.findByContentId(dto.getContentId())
                .orElseGet(() -> festivalRepository.save(dto.toFestivalEntity()));

        if (bookmarkRepository.existsByUser_UserIdAndFestival_FestivalId(userId, festival.getFestivalId())) {
            throw new IllegalStateException("이미 찜한 축제입니다.");
        }

        BookmarkEntity bookmark = BookmarkEntity.builder()
                .user(userRepository.getReferenceById(userId))
                .festival(festival)
                .userMemo(dto.getUserMemo())
                .build();
        bookmarkRepository.save(bookmark);
    }

    public List<BookmarkResponseDTO> getBookmark(Long tempUserId) {
        return bookmarkRepository.findAllByUser_UserId(tempUserId)
                .stream()
                .map(BookmarkResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void putBookmark(Long userId, Long bookmarkId, BookmarkUpdateRequestDTO request) {
        BookmarkEntity bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다."));

        if (!bookmark.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 북마크만 수정할 수 있습니다.");
        }
        bookmark.updateUserMemo(request.getUserMemo());
    }

    public void deleteBookmark(Long tempUserId, Long bookmarkId) {
        BookmarkEntity bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new RuntimeException("북마크를 찾을 수 없습니다."));

        if (!bookmark.getUser().getUserId().equals(tempUserId)) {
            throw new IllegalStateException("본인의 북마크만 삭제할 수 있습니다.");
        }

        bookmarkRepository.delete(bookmark);
    }
}
