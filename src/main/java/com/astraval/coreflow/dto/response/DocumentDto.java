package com.astraval.coreflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {
    private String doc_id;
    private String doc_code;
    private String doc_name;
}

/* - Sample JOSN
{
    "doc_id": "doc-02",
    "doc_code": "PAN",
    "doc_name": "PAN"
}

*/