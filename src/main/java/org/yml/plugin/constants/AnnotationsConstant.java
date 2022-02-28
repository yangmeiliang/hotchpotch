package org.yml.plugin.constants;

/**
 * 注解包路径
 */
@SuppressWarnings("ALL")
public interface AnnotationsConstant {

    String RequestMapping = "org.springframework.web.bind.annotation.RequestMapping";
    String GetMapping = "org.springframework.web.bind.annotation.GetMapping";
    String PostMapping = "org.springframework.web.bind.annotation.PostMapping";
    String PutMapping = "org.springframework.web.bind.annotation.PutMapping";
    String DeleteMapping = "org.springframework.web.bind.annotation.DeleteMapping";
    String PatchMapping = "org.springframework.web.bind.annotation.PatchMapping";

    String RequestBody = "RequestBody";
    String RequestParam = "RequestParam";
    String RequestHeader = "RequestHeader";
    String RequestAttribute = "RequestAttribute";
    String PathVariable = "PathVariable";

    String NotBlank = "NotBlank";
    String NotNull = "NotNull";
    String NotEmpty = "NotEmpty";

}
