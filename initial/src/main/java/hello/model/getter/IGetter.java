package hello.model.getter;

import hello.enums.FormatEnum;

import java.io.Serializable;

public interface IGetter extends Serializable {
    String getClazz();
    String getEndpointId();
    String getFuncParamByWebParam(String webParam);
    String getExecutedFunctionName();
    FormatEnum getResultType();
}
