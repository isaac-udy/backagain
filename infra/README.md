# Infrastructure

One Cloud Run service, one Cloud SQL database, two secrets, and a domain, managed by two
Terraform stacks:

| Stack | Applied by | Holds |
| --- | --- | --- |
| [`bootstrap`](terraform/bootstrap) | A person, once | The APIs, the Terraform state bucket, the image repository, and the Workload Identity Federation trust between this repository and Google |
| [`main`](terraform/main) | GitHub Actions, on every push to `main` | The Cloud Run service, Cloud SQL, Secret Manager, and the domain mapping |

```
git push ─► Actions: build ─► Actions: deploy ─► Artifact Registry
                              (WIF, no key)  └─► terraform apply ─► Cloud Run ─► Cloud SQL
                                                                      ▲
                                                        Secret Manager┘
```

## First deploy

You need a GCP project with billing enabled, Owner on it, `gcloud`, and Terraform 1.9 or newer.

### 1. Bootstrap

The state bucket is declared by the stack that stores its state in it, so it is created by hand
once and then imported:

```sh
export PROJECT_ID=your-project-id
export STATE_BUCKET=$PROJECT_ID-terraform-state
export REGION=asia-southeast1

gcloud services enable cloudresourcemanager.googleapis.com storage.googleapis.com --project "$PROJECT_ID"
gcloud storage buckets create "gs://$STATE_BUCKET" --project "$PROJECT_ID" --location "$REGION" \
  --uniform-bucket-level-access --public-access-prevention
gcloud storage buckets update "gs://$STATE_BUCKET" --versioning

cd infra/terraform/bootstrap
cp terraform.tfvars.example terraform.tfvars   # then fill it in
terraform init -backend-config="bucket=$STATE_BUCKET" -backend-config="prefix=bootstrap"
terraform import google_storage_bucket.terraform_state "$STATE_BUCKET"
terraform apply
```

### 2. GitHub

Create an environment called `production` in the repository settings, and give it:

| Kind | Name | Value |
| --- | --- | --- |
| Variable | `GCP_PROJECT_ID` | the project |
| Variable | `GCP_REGION` | `asia-southeast1` |
| Variable | `GCP_WORKLOAD_IDENTITY_PROVIDER` | `terraform output -raw workload_identity_provider` |
| Variable | `GCP_DEPLOYER_SERVICE_ACCOUNT` | `terraform output -raw deployer_service_account` |
| Variable | `TF_STATE_BUCKET` | `terraform output -raw terraform_state_bucket` |
| Secret | `PRESENTER_PASSWORD` | anything you'll remember on stage |

Optionally, `MIN_INSTANCES` set to `1` to keep an instance warm, so the first visitor after a quiet
spell doesn't wait for a cold start.

### 3. Deploy

Push to `main`, or run the **Deploy** workflow by hand. The summary prints the service's run.app
address.

### 4. The domain

A Cloud Run domain mapping can only be created by a verified owner of the domain, and the deploy
creates it as the deployer service account. Once:

1. Verify the domain in [Google Search Console](https://search.google.com/search-console) with your
   own account.
2. In the property's settings, add the deployer service account's email as an owner.
3. Set the environment variable `DOMAIN` to the domain, say `backagain.example.com`, and re-run the
   deploy.

Then create the records from:

```sh
cd infra/terraform/main
terraform init -backend-config="bucket=$STATE_BUCKET" -backend-config="prefix=main"
terraform output domain_records
```

Cloud Run issues the certificate once the records resolve, which can take an hour.

## Operating it

- **Presenting.** Open `https://your-domain/present` on your phone and sign in; open
  `https://your-domain/?stage` on the projector. Go live from the phone.
- **Rotating the presenter password.** Change the GitHub secret and re-run the deploy. Existing
  sessions stay valid until they expire; `DELETE FROM presenter_sessions` ends them early.
- **Don't deploy during a talk.** For the few seconds a new revision takes over, two instances run
  side by side with a live hub each, and followers can miss a move.
- **Bandwidth.** Outbound data is billed with no ceiling, so the server stops sending the web bundle
  after 10 GB in an hour or 25 GB in a day, answering 503 and logging a warning until the window
  resets. The API and live updates keep working for anyone already on the deck. The limits are in
  `app/server/.../Server.kt`. A billing budget alert is worth setting too.
- **Between talks.** Run the **Pause or resume** workflow with `paused`. It turns the service off
  and stops the database, leaving only storage to pay for, and the address answers "Service
  unavailable" until you run it again with `live`. Starting the database takes about a quarter of
  an hour, so resume well before going on. A deploy while paused wakes it the same way, since Cloud
  Run starts a new revision before accepting it, and pauses it again after.
- **Afterwards.** Left running, the service scales to zero between visitors and the database is
  the only thing still billing. To remove everything, set
  `database_deletion_protection = false`, apply, then `terraform destroy` in `main`.
