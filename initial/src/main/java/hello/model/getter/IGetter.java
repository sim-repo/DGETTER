package hello.model.getter;

import hello.enums.FormatEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public interface IGetter extends Serializable {
    Integer getId();
    String getClazz();
    String getEndpointId();
    String getFuncParamByWebParam(String webParam);
    String getExecutedFunctionName();
    FormatEnum getResultType();
    Boolean getAllAccess();
    HashSet<String> getRoles();
}
