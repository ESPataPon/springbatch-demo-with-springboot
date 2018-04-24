package batch;

import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class Person implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String id;

    @Size(max = 4, min = 2) // 此处使用JSR-303注解来校验数据
    private String name;

    private Integer age;

    private String nation;

    private String address;

}