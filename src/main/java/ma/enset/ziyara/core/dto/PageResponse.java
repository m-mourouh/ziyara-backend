package ma.enset.ziyara.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    // Additional computed fields for frontend
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;

    public boolean isHasNext() {
        return !last;
    }

    public boolean isHasPrevious() {
        return !first;
    }

    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
}