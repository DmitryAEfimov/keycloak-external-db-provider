CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

DROP TABLE IF EXISTS "public"."pros";
DROP TABLE IF EXISTS "public"."spaces";
DROP TABLE IF EXISTS "public"."accounts";
DROP TABLE IF EXISTS "public"."users";

CREATE TABLE "public"."users" (
    "id" uuid NOT NULL DEFAULT gen_random_uuid (),
    "username" text NOT NULL,
    "email" public.citext,
    "password" text NOT NULL,
    "first_name" text,
    "last_name" text,
    "display_name" text GENERATED ALWAYS AS (
        CASE WHEN ("first_name" IS NOT NULL
            AND "first_name" != '')
            AND("last_name" IS NOT NULL
                AND "last_name" != '') THEN
            "first_name" || ' ' ||
        LEFT("last_name", 1) || '.'
        END) STORED,
    "sex" text NOT NULL DEFAULT 'decline' CONSTRAINT "users_sex_check" CHECK (sex::text = ANY (ARRAY [
 	      'male'::character varying,
 	      'female'::character varying,
 	      'decline'::character varying
 	    ]::text [])),
    "avatar" text,
    "phone" text,
    "settings" jsonb NOT NULL DEFAULT jsonb_build_object (),
    "last_login" timestamptz,
    "created_at" timestamptz NOT NULL DEFAULT now(),
    "updated_at" timestamptz NOT NULL DEFAULT now(),
    "deleted_at" timestamptz,
    CONSTRAINT proper_email CHECK ((email OPERATOR (public. ~*) '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'::public.citext)),
    PRIMARY KEY ("id"),
    UNIQUE ("username"),
    UNIQUE ("email")
);

CREATE TABLE "public"."accounts" (
     "id" uuid NOT NULL DEFAULT gen_random_uuid(),
      "user_id" uuid NOT NULL,
      "active" boolean DEFAULT false NOT NULL,
      "account_type" text NOT NULL DEFAULT 'regular' constraint "valid_account_type_check"
  check (account_type::text = ANY (
    ARRAY[
      'regular'::character varying,
      'space'::character varying,
      'pro'::character varying
    ]::text[]
  )),
      "created_at" timestamp with time zone DEFAULT now() NOT NULL,
      "updated_at" timestamp with time zone DEFAULT now() NOT NULL,
      "deleted_at" timestamptz,
      PRIMARY KEY ("id")
  );

CREATE TABLE "public"."spaces" (
	"id" uuid NOT NULL DEFAULT gen_random_uuid(),
	"account_id" uuid NOT NULL,
	"name" TEXT NOT NULL,
	"slug" TEXT NOT NULL,
	"description" TEXT,
	PRIMARY KEY ("id"),
	UNIQUE("slug"),
	FOREIGN KEY ("account_id") REFERENCES "public"."accounts"("id")
        ON UPDATE restrict
        ON DELETE restrict
);


CREATE TABLE "public"."pros" (
	"id" uuid NOT NULL DEFAULT gen_random_uuid(),
	"account_id" uuid NOT NULL,
	"name" TEXT NOT NULL,
	"settings" jsonb NOT NULL DEFAULT jsonb_build_object(),
	PRIMARY KEY ("id"),
	FOREIGN KEY ("account_id") REFERENCES "public"."accounts"("id")
        ON UPDATE restrict
        ON DELETE restrict
);


INSERT INTO "public"."users" (id, "username", "email", "password", first_name, last_name, sex) VALUES
('5e93d778-21dd-4731-b239-0b1c5fad65b0', 'kevina', 'kevinalexander@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Kevin', 'Alexander', 'decline'),
('d1801502-a252-4d23-87da-2ea111b4a0d5', 'emilly', 'emillyfg@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Emilly', 'Fernandes Gomes', 'female'),
('ff7f344c-ffbc-491b-8ef9-0eb6a18519bd', 'hartr', 'ronaldhart@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Ronald', 'Hart', 'male'),
('d9c4813b-bffe-47a2-9363-30d9a4f4791a', 'feng', 'pingfeng@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Ping', 'Feng', 'female'),
('5c8aecd8-db97-4819-b832-4b2fa44a635f', 'burhan', 'burhan@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Ishaq Burhan', 'Bitar', 'male'),
('37b0e7f1-9216-45be-9a35-4a575d9cf3bb', 'tonyb', 'tonyb@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', '', '', 'decline'),
('a30b84b5-f9b6-4ae4-9892-ecabb7f8748b', 'philip', 'philipuvarov@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Philip', 'Uvarov', 'male'),
('d7b93e4f-e32a-416d-bf0f-29eec8595c64', 'miro', 'miroslav@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Miroslav', 'Sergeyeva', 'decline'),
('be36946a-9d5b-48be-938c-9c435d7fca78', 'katrin', 'katrin@examplemail.com', '$2y$10$YPfU5w2xgCq4CvHSEv0yie.GFyNq5NmOv6EC7L1UUfUKWstpKhRrm', 'Katrin', 'Rothschild', 'female');

-- hashed password is: secret
INSERT INTO "public"."accounts" (id, user_id, active, account_type) VALUES
('3e7925f0-3da8-4a7d-8dca-d9785326aa78', '5e93d778-21dd-4731-b239-0b1c5fad65b0', true, 'space'),
('e8edf0ed-2e11-4dd5-9db7-d59197160ae8', 'd1801502-a252-4d23-87da-2ea111b4a0d5', true, 'space'),
('f61f7324-d452-4f99-9fbe-f8b783242df3', 'ff7f344c-ffbc-491b-8ef9-0eb6a18519bd', true, 'regular'),
('55d39d30-05e4-478b-96ef-b7a1b55a86cc', 'd9c4813b-bffe-47a2-9363-30d9a4f4791a', false, 'space'),
('e19fc10e-a41e-4028-97c1-47b4948713e8', 'd9c4813b-bffe-47a2-9363-30d9a4f4791a', true, 'regular'),
('f4417a9f-d4bf-4c35-9089-9453c394c0e9', '5c8aecd8-db97-4819-b832-4b2fa44a635f', true, 'regular'),
('9540472b-3584-4390-9d7c-92eadb5db4de', '5c8aecd8-db97-4819-b832-4b2fa44a635f', true, 'pro'),
('49059beb-2513-4dce-af94-b617e24162cf', '37b0e7f1-9216-45be-9a35-4a575d9cf3bb', true, 'regular'),
('b33c3dc6-34a9-49b8-b3b3-957fc3573ac6', 'a30b84b5-f9b6-4ae4-9892-ecabb7f8748b', false, 'space'),
('036df7f8-5e41-4123-bf92-cae2f273b14c', 'd7b93e4f-e32a-416d-bf0f-29eec8595c64', true, 'space'),
('a09f2d8f-78a1-45b5-b74c-e0e307342336', 'be36946a-9d5b-48be-938c-9c435d7fca78', true, 'space');

INSERT INTO "public"."spaces" (id, account_id, "name", "slug", "description") VALUES
('d0f1ef1f-8d96-40b2-9555-bc9955c9ff40', '3e7925f0-3da8-4a7d-8dca-d9785326aa78', 'Kev Space', 'kevspace', 'This is example Space'),
('4685bbfa-dce7-4302-a5c2-9a250e558181', 'e8edf0ed-2e11-4dd5-9db7-d59197160ae8', 'Emilly Space', 'emspace', NULL),
('de917588-d8bf-4a24-8c33-656435cd0464', '55d39d30-05e4-478b-96ef-b7a1b55a86cc', 'Ping Space', 'pingspace', NULL),
('bfee024b-176d-4829-aded-17c86d1e3a09', 'b33c3dc6-34a9-49b8-b3b3-957fc3573ac6', 'Philip Space', 'pspace', NULL),
('ae88206e-b065-4d34-a1ed-a45ffc5223db', '036df7f8-5e41-4123-bf92-cae2f273b14c', 'Miro 1', 'miro1', 'Space One'),
('b6b82243-ba66-4dcc-9eed-da95a5a1b0fa', '036df7f8-5e41-4123-bf92-cae2f273b14c', 'Miro 2', 'miro2', 'Space Two'),
('4c506925-923b-41da-a684-b1fca35a5541', 'a09f2d8f-78a1-45b5-b74c-e0e307342336', 'Kats Space', 'katspace', NULL);

INSERT INTO "public"."pros" (id, account_id, "name") VALUES
('8530bd3d-5c15-4b41-a2b9-de4d521a32f7', '9540472b-3584-4390-9d7c-92eadb5db4de', 'BPro 1'),
('888d42e4-d013-4a0d-a69f-3273c44db308', '9540472b-3584-4390-9d7c-92eadb5db4de', 'BPro 2');
