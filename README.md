<hr>
<h3>사용법</h3>

<p>BomGenerator.createBom("url") -> Bom Object 리턴</p>

IllegalGithubURLException -> 요청한 url 이 github 링크가 아닐때 <br>
OptionFileNotFoundException -> 요청한 url 에서 pom.xml 이 발견되지 않았을때 <br>
KeyType -> 설정 파일의 세부 옵션을 거르기 위함