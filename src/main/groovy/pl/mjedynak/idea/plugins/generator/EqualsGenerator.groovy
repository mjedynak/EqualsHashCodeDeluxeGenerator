package pl.mjedynak.idea.plugins.generator

import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.annotations.NotNull

class EqualsGenerator {

    PsiMethod equalsMethod(@NotNull List<PsiField> equalsPsiFields, PsiClass psiClass, String equalsMethodName) {
        if (!equalsPsiFields.isEmpty()) {
            PsiElementFactory factory = getFactory(equalsPsiFields[0])
            StringBuilder methodText = new StringBuilder()
            methodText << '@Override public boolean equals(Object obj) {'
            methodText << ' if (this == obj) {return true;}'
            methodText << ' if (obj == null || getClass() != obj.getClass()) {return false;}'
            methodText << " final ${psiClass.name} other = (${psiClass.name}) obj;"
            methodText << ' return '
            equalsPsiFields.eachWithIndex { field, index ->
                methodText <<  "Objects.${equalsMethodName}(this.${field.name}, other.${field.name})"
                if (index < equalsPsiFields.size() - 1) {
                    methodText << ' && '
                }
            }
            methodText << ';}'
            factory.createMethodFromText(methodText.toString(), null, LanguageLevel.JDK_1_6)
        }
    }

    private PsiElementFactory getFactory(PsiField psiField) {
        JavaPsiFacade.getInstance(psiField.project).elementFactory
    }
}


