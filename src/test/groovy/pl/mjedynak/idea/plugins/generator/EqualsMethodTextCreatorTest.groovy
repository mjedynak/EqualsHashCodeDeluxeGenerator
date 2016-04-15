package pl.mjedynak.idea.plugins.generator

import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import pl.mjedynak.idea.plugins.model.EqualsAndHashCodeType
import pl.mjedynak.idea.plugins.psi.EqualsMethodFinder
import pl.mjedynak.idea.plugins.psi.ParentClassChecker
import spock.lang.Specification

class EqualsMethodTextCreatorTest extends Specification {

    ParentClassChecker parentClassChecker = Mock()
    EqualsMethodFinder finder = Mock()
    EqualsMethodTextCreator equalsMethodTextCreator = new EqualsMethodTextCreator(parentClassChecker, finder)
    PsiField psiField = Mock()
    PsiField psiField2 = Mock()
    PsiClass psiClass = Mock()
    String type = 'Object'
    String fieldName = 'field'
    EqualsAndHashCodeType equalsAndHashCodeType = EqualsAndHashCodeType.JAVA_7

    def setup() {
        psiClass.name >> type
        psiClass.extendsListTypes >> []
        psiField.name >> fieldName
    }

    def "creates equals method for one field"() {
        when:
        def result = equalsMethodTextCreator.createMethodText([psiField], psiClass, equalsAndHashCodeType)

        then:
        result == '@Override public boolean equals(Object obj) { if (this == obj) {return true;} ' +
                'if (obj == null || getClass() != obj.getClass()) {return false;} ' +
                'final Object other = (Object) obj; return Objects.equals(this.field, other.field);}'
    }

    def "creates equals method with special call when field is array"() {
        psiField.type >> Mock(PsiArrayType)

        when:
        def result = equalsMethodTextCreator.createMethodText([psiField], psiClass, equalsAndHashCodeType)

        then:
        result == '@Override public boolean equals(Object obj) { if (this == obj) {return true;} ' +
                'if (obj == null || getClass() != obj.getClass()) {return false;} ' +
                'final Object other = (Object) obj; return Objects.deepEquals(this.field, other.field);}'
    }

    def "creates equals method with super call when parent class checker says so"() {
        parentClassChecker.hasClassWithOverriddenMethodInInheritanceHierarchy(finder, psiClass) >> true

        when:
        def result = equalsMethodTextCreator.createMethodText([psiField], psiClass, equalsAndHashCodeType)

        then:
        result == '@Override public boolean equals(Object obj) { if (this == obj) {return true;} ' +
                'if (obj == null || getClass() != obj.getClass()) {return false;} ' +
                'final Object other = (Object) obj; return super.equals(obj)\n && Objects.equals(this.field, other.field);}'
    }

    def "creates equals method for two fields"() {
        String fieldName2 = 'anotherField'
        psiField2.name >> fieldName2

        when:
        def result = equalsMethodTextCreator.createMethodText([psiField, psiField2], psiClass, equalsAndHashCodeType)

        then:
        result == '@Override public boolean equals(Object obj) { if (this == obj) {return true;} ' +
                'if (obj == null || getClass() != obj.getClass()) {return false;} ' +
                'final Object other = (Object) obj; return Objects.equals(this.field, other.field)\n && Objects.equals(this.anotherField, other.anotherField);}'
    }
}
