package actors

import com.gilt.apidoc.models.{Membership, Publication, Service}
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import db.{Authorization, MembershipsDao, MembershipRequestsDao, OrganizationsDao, ServicesDao}
import lib.{Email, Pager, Person, Role}
import akka.actor._
import play.api.Logger
import play.api.Play.current
import java.util.UUID

object EmailActor {

  object Messages {
    case class MembershipRequestCreated(guid: UUID)
    case class MembershipCreated(guid: UUID)
    case class ServiceCreated(guid: UUID)
  }

}

class EmailActor extends Actor {

  def receive = {

    case EmailActor.Messages.MembershipRequestCreated(guid) => Util.withVerboseErrorHandler(
      s"EmailActor.Messages.MembershipRequestCreated($guid)", {
        MembershipRequestsDao.findByGuid(Authorization.All, guid).map { request =>
          Emails.deliver(
            org = request.organization,
            publication = Publication.MembershipRequestsCreate,
            subject = s"${request.organization.name}: Membership Request from ${request.user.email}",
            body = views.html.emails.membershipRequestCreated(request).toString
          )
        }
      }
    )

    case EmailActor.Messages.MembershipCreated(guid) => Util.withVerboseErrorHandler(
      s"EmailActor.Messages.MembershipCreated($guid)", {
        MembershipsDao.findByGuid(Authorization.All, guid).map { membership =>
          Emails.deliver(
            org = membership.organization,
            publication = Publication.MembershipsCreate,
            subject = s"${membership.organization.name}: ${membership.user.email} has joined as ${membership.role}",
            body = views.html.emails.membershipCreated(membership).toString
          )
        }
      }
    )

    case EmailActor.Messages.ServiceCreated(guid) => Util.withVerboseErrorHandler(
      s"EmailActor.Messages.ServiceCreated($guid)", {
        ServicesDao.findByGuid(Authorization.All, guid).map { service =>
          OrganizationsDao.findAll(Authorization.All, service = Some(service)).map { org =>
            Emails.deliver(
              org = org,
              publication = Publication.ServicesCreate,
              subject = s"${org.name}: New Service Created - ${service.name}",
              body = views.html.emails.serviceCreated(org, service).toString
            )
          }
        }
      }
    )

    case m: Any => {
      Logger.error("Email actor got an unhandled message: " + m)
    }

  }
}

