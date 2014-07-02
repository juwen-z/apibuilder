package core

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSpec}
import org.scalatest.Matchers

class SvcApiDocJson extends FunSpec with Matchers {

  private val Path = "api/api.json"
  private lazy val service = TestHelper.parseFile(Path).serviceDescription.get

  it("alphabetizes models") {
    service.models.map(_.name) should be(service.models.map(_.name).sorted)
  }

  it("alphabetizes resources") {
    service.resources.map(_.model.plural) should be(service.resources.map(_.model.plural).sorted)
  }

  it("parses models") {
    service.models.map(_.name).mkString(" ") should be("code error membership membership_request organization service user user_authentication version")

    val user = service.models.find(_.name == "user").get
    user.fields.map(_.name).mkString(" ") should be("guid email name image_url")
    user.fields.find(_.name == "guid").get.fieldtype.asInstanceOf[PrimitiveFieldType].datatype.name should be("uuid")
    user.fields.find(_.name == "email").get.fieldtype.asInstanceOf[PrimitiveFieldType].datatype.name should be("string")
  }

  it("parses resources") {
    service.resources.map(_.model.name).sorted.mkString(" ") should be("code membership membership_request organization service user user_authentication version")
  }

  it("has defaults for all limit and offset parameters") {
    service.resources.flatMap(_.operations.filter(_.method == "GET")).foreach { op =>

      op.parameters.find { _.name == "limit" } match {
        case None => {}
        case Some(p: Parameter) => {
          p.default should be(Some("25"))
        }
      }

      op.parameters.find { _.name == "offset" } match {
        case None => {}
        case Some(p: Parameter) => {
          p.default should be(Some("0"))
        }
      }

    }
  }

}
