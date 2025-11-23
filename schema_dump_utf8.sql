--
-- PostgreSQL database dump
--

\restrict jOgaxtR0ELMolnPhEK9LloeYkZVRp8mxffhOT8nbTSLaAbdjCJUxE78SlaYdTTW

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: admins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.admins (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    full_name character varying(255),
    is_active boolean NOT NULL,
    password character varying(255) NOT NULL,
    permissions text,
    updated_at timestamp(6) without time zone,
    username character varying(100) NOT NULL
);


--
-- Name: cart_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cart_items (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    quantity integer NOT NULL,
    cart_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT cart_items_quantity_check CHECK ((quantity >= 1))
);


--
-- Name: carts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.carts (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: conversations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversations (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    is_active boolean NOT NULL,
    title character varying(255),
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: deliveries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.deliveries (
    id uuid NOT NULL,
    actual_delivery timestamp(6) without time zone,
    address text,
    created_at timestamp(6) without time zone,
    current_location text,
    delivery_notes text,
    driver_name character varying(255),
    driver_phone character varying(20),
    estimated_delivery timestamp(6) without time zone,
    pickup_time timestamp(6) without time zone,
    status character varying(50) NOT NULL,
    tracking_number character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    order_id uuid NOT NULL
);


--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    attachment_url character varying(255),
    content text NOT NULL,
    created_at timestamp(6) without time zone,
    is_read boolean NOT NULL,
    sender_id uuid,
    sender_type character varying(50) NOT NULL,
    conversation_id uuid NOT NULL
);


--
-- Name: order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.order_items (
    id uuid NOT NULL,
    price numeric(10,2) NOT NULL,
    quantity integer NOT NULL,
    order_id uuid NOT NULL,
    product_id uuid NOT NULL,
    CONSTRAINT order_items_quantity_check CHECK ((quantity >= 1))
);


--
-- Name: orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.orders (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    delivered_at timestamp(6) without time zone,
    delivery_address text,
    notes text,
    payment_method character varying(50),
    payment_status character varying(50),
    status character varying(50) NOT NULL,
    total_price numeric(10,2) NOT NULL,
    tracking_number character varying(255),
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payments (
    id uuid NOT NULL,
    amount numeric(10,2) NOT NULL,
    card_brand character varying(50),
    card_last4 character varying(4),
    created_at timestamp(6) without time zone,
    currency character varying(3) NOT NULL,
    failure_message text,
    metadata text,
    payment_method character varying(50),
    receipt_url character varying(500),
    refund_amount numeric(10,2),
    refund_reason text,
    refunded_at timestamp(6) without time zone,
    status character varying(50) NOT NULL,
    stripe_payment_intent_id character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    order_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT payments_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'PROCESSING'::character varying, 'SUCCEEDED'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying, 'REQUIRES_ACTION'::character varying, 'REFUNDED'::character varying, 'PARTIALLY_REFUNDED'::character varying])::text[])))
);


--
-- Name: products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.products (
    id uuid NOT NULL,
    brand character varying(100),
    category character varying(100),
    compatibility text,
    created_at timestamp(6) without time zone,
    description text,
    image_url text,
    model character varying(100),
    name character varying(255) NOT NULL,
    price numeric(10,2) NOT NULL,
    stock integer NOT NULL,
    updated_at timestamp(6) without time zone,
    year integer,
    CONSTRAINT products_stock_check CHECK ((stock >= 0))
);


--
-- Name: purchase_order_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.purchase_order_items (
    id uuid NOT NULL,
    notes text,
    quantity integer NOT NULL,
    received_quantity integer,
    subtotal numeric(12,2) NOT NULL,
    unit_price numeric(10,2) NOT NULL,
    product_id uuid NOT NULL,
    purchase_order_id uuid NOT NULL,
    CONSTRAINT purchase_order_items_quantity_check CHECK ((quantity >= 1))
);


--
-- Name: purchase_orders; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.purchase_orders (
    id uuid NOT NULL,
    actual_delivery_date date,
    approved_by character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    created_by character varying(255),
    discount_amount numeric(12,2),
    expected_delivery_date date,
    grand_total numeric(12,2) NOT NULL,
    notes text,
    order_date date NOT NULL,
    po_number character varying(255) NOT NULL,
    received_by character varying(255),
    shipping_cost numeric(12,2),
    status character varying(255) NOT NULL,
    tax_amount numeric(12,2),
    total_amount numeric(12,2) NOT NULL,
    updated_at timestamp(6) without time zone,
    supplier_id uuid NOT NULL,
    CONSTRAINT purchase_orders_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'PENDING'::character varying, 'APPROVED'::character varying, 'ORDERED'::character varying, 'SHIPPED'::character varying, 'RECEIVED'::character varying, 'CANCELLED'::character varying, 'COMPLETED'::character varying])::text[])))
);


--
-- Name: reclamations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reclamations (
    id uuid NOT NULL,
    attachment_url character varying(255),
    created_at timestamp(6) without time zone,
    description text NOT NULL,
    resolved_at timestamp(6) without time zone,
    response text,
    status character varying(50) NOT NULL,
    subject character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: recommendations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recommendations (
    id uuid NOT NULL,
    ai_response text,
    confidence_score double precision,
    created_at timestamp(6) without time zone,
    image_url text,
    suggested_products text,
    symptoms text,
    user_id uuid NOT NULL
);


--
-- Name: reorder_settings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reorder_settings (
    id uuid NOT NULL,
    auto_reorder boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_enabled boolean NOT NULL,
    last_alert_sent timestamp(6) without time zone,
    last_reorder_date timestamp(6) without time zone,
    lead_time_days integer,
    maximum_stock integer,
    minimum_stock integer,
    notes text,
    reorder_point integer NOT NULL,
    reorder_quantity integer NOT NULL,
    updated_at timestamp(6) without time zone,
    preferred_supplier_id uuid,
    product_id uuid NOT NULL,
    CONSTRAINT reorder_settings_lead_time_days_check CHECK ((lead_time_days >= 1)),
    CONSTRAINT reorder_settings_maximum_stock_check CHECK ((maximum_stock >= 1)),
    CONSTRAINT reorder_settings_minimum_stock_check CHECK ((minimum_stock >= 0)),
    CONSTRAINT reorder_settings_reorder_point_check CHECK ((reorder_point >= 0)),
    CONSTRAINT reorder_settings_reorder_quantity_check CHECK ((reorder_quantity >= 1))
);


--
-- Name: reports; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reports (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    data text,
    description text,
    file_url character varying(255),
    generated_by uuid,
    report_type character varying(100) NOT NULL,
    title character varying(255)
);


--
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying(50) NOT NULL
);


--
-- Name: roles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

ALTER TABLE public.roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: stock_alerts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_alerts (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    product_id uuid NOT NULL,
    threshold integer DEFAULT 5 NOT NULL,
    alert_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: stock_movements; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stock_movements (
    id uuid NOT NULL,
    movement_date timestamp(6) without time zone NOT NULL,
    movement_type character varying(255) NOT NULL,
    new_stock integer NOT NULL,
    notes text,
    performed_by character varying(255),
    previous_stock integer NOT NULL,
    quantity integer NOT NULL,
    reference_id uuid,
    reference_type character varying(255),
    product_id uuid NOT NULL,
    CONSTRAINT stock_movements_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['PURCHASE'::character varying, 'SALE'::character varying, 'RETURN_FROM_CUSTOMER'::character varying, 'RETURN_TO_SUPPLIER'::character varying, 'ADJUSTMENT'::character varying, 'DAMAGED'::character varying, 'TRANSFER'::character varying, 'INITIAL'::character varying])::text[])))
);


--
-- Name: super_admins; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.super_admins (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    full_name character varying(255),
    is_active boolean NOT NULL,
    password character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    username character varying(100) NOT NULL
);


--
-- Name: supplier_products; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.supplier_products (
    supplier_id uuid NOT NULL,
    product_id uuid NOT NULL
);


--
-- Name: suppliers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.suppliers (
    id uuid NOT NULL,
    address character varying(255),
    city character varying(255),
    company_name character varying(255),
    contact_person character varying(255),
    country character varying(255),
    created_at timestamp(6) without time zone NOT NULL,
    email character varying(255),
    is_active boolean NOT NULL,
    name character varying(255) NOT NULL,
    notes text,
    payment_terms character varying(255),
    phone character varying(255),
    postal_code character varying(255),
    rating double precision,
    tax_id character varying(255),
    updated_at timestamp(6) without time zone,
    website character varying(255)
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    address text,
    created_at timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    full_name character varying(255),
    password character varying(255) NOT NULL,
    phone character varying(20),
    updated_at timestamp(6) without time zone,
    username character varying(100) NOT NULL,
    role_id integer NOT NULL,
    email_verification_token character varying(255),
    email_verification_token_expiry timestamp(6) without time zone,
    password_reset_token character varying(255),
    password_reset_token_expiry timestamp(6) without time zone,
    is_email_verified boolean DEFAULT true NOT NULL
);


--
-- Name: vehicles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vehicles (
    id uuid NOT NULL,
    brand character varying(100) NOT NULL,
    created_at timestamp(6) without time zone,
    model character varying(100) NOT NULL,
    year integer NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT vehicles_year_check CHECK ((year >= 1900))
);


--
-- Name: admins admins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admins
    ADD CONSTRAINT admins_pkey PRIMARY KEY (id);


--
-- Name: cart_items cart_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);


--
-- Name: carts carts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT carts_pkey PRIMARY KEY (id);


--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);


--
-- Name: deliveries deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT deliveries_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: order_items order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT order_items_pkey PRIMARY KEY (id);


--
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);


--
-- Name: products products_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);


--
-- Name: purchase_order_items purchase_order_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_order_items
    ADD CONSTRAINT purchase_order_items_pkey PRIMARY KEY (id);


--
-- Name: purchase_orders purchase_orders_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT purchase_orders_pkey PRIMARY KEY (id);


--
-- Name: reclamations reclamations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reclamations
    ADD CONSTRAINT reclamations_pkey PRIMARY KEY (id);


--
-- Name: recommendations recommendations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recommendations
    ADD CONSTRAINT recommendations_pkey PRIMARY KEY (id);


--
-- Name: reorder_settings reorder_settings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reorder_settings
    ADD CONSTRAINT reorder_settings_pkey PRIMARY KEY (id);


--
-- Name: reports reports_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reports
    ADD CONSTRAINT reports_pkey PRIMARY KEY (id);


--
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- Name: stock_alerts stock_alerts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_alerts
    ADD CONSTRAINT stock_alerts_pkey PRIMARY KEY (id);


--
-- Name: stock_movements stock_movements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_movements
    ADD CONSTRAINT stock_movements_pkey PRIMARY KEY (id);


--
-- Name: super_admins super_admins_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admins
    ADD CONSTRAINT super_admins_pkey PRIMARY KEY (id);


--
-- Name: suppliers suppliers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.suppliers
    ADD CONSTRAINT suppliers_pkey PRIMARY KEY (id);


--
-- Name: super_admins uk106g1eh84uk6c4bs7dcssubwi; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admins
    ADD CONSTRAINT uk106g1eh84uk6c4bs7dcssubwi UNIQUE (username);


--
-- Name: admins uk47bvqemyk6vlm0w7crc3opdd4; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admins
    ADD CONSTRAINT uk47bvqemyk6vlm0w7crc3opdd4 UNIQUE (email);


--
-- Name: carts uk64t7ox312pqal3p7fg9o503c2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT uk64t7ox312pqal3p7fg9o503c2 UNIQUE (user_id);


--
-- Name: users uk6dotkott2kjsp8vw4d0m25fb7; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);


--
-- Name: reorder_settings ukd1fcp8rsuoy6ylcreslr3phyk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reorder_settings
    ADD CONSTRAINT ukd1fcp8rsuoy6ylcreslr3phyk UNIQUE (product_id);


--
-- Name: deliveries ukk36n9p5v7dd96hpgkwybvbogt; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT ukk36n9p5v7dd96hpgkwybvbogt UNIQUE (order_id);


--
-- Name: admins ukmi8vkhus4xbdbqcac2jm4spvd; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.admins
    ADD CONSTRAINT ukmi8vkhus4xbdbqcac2jm4spvd UNIQUE (username);


--
-- Name: roles ukofx66keruapi6vyqpv6f2or37; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT ukofx66keruapi6vyqpv6f2or37 UNIQUE (name);


--
-- Name: purchase_orders ukpbiykvcpyg0jslne4gviyeuc2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT ukpbiykvcpyg0jslne4gviyeuc2 UNIQUE (po_number);


--
-- Name: payments ukpuc8mkpduwb4ws7khxcoo0s3t; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT ukpuc8mkpduwb4ws7khxcoo0s3t UNIQUE (stripe_payment_intent_id);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: super_admins ukrfu7qxpd0salskd48dufab90y; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.super_admins
    ADD CONSTRAINT ukrfu7qxpd0salskd48dufab90y UNIQUE (email);


--
-- Name: deliveries uksxjjhx1w78xjss4g25gahe5s9; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT uksxjjhx1w78xjss4g25gahe5s9 UNIQUE (tracking_number);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: vehicles vehicles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicles
    ADD CONSTRAINT vehicles_pkey PRIMARY KEY (id);


--
-- Name: idx_cart_items_cart_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_items_cart_id ON public.cart_items USING btree (cart_id);


--
-- Name: idx_cart_items_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cart_items_product_id ON public.cart_items USING btree (product_id);


--
-- Name: idx_carts_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_carts_user_id ON public.carts USING btree (user_id);


--
-- Name: idx_conversations_updated_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversations_updated_at ON public.conversations USING btree (updated_at);


--
-- Name: idx_conversations_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversations_user_id ON public.conversations USING btree (user_id);


--
-- Name: idx_deliveries_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_deliveries_order_id ON public.deliveries USING btree (order_id);


--
-- Name: idx_deliveries_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_deliveries_status ON public.deliveries USING btree (status);


--
-- Name: idx_messages_conversation_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_conversation_id ON public.messages USING btree (conversation_id);


--
-- Name: idx_messages_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_created_at ON public.messages USING btree (created_at);


--
-- Name: idx_order_items_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_order_id ON public.order_items USING btree (order_id);


--
-- Name: idx_order_items_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_order_items_product_id ON public.order_items USING btree (product_id);


--
-- Name: idx_orders_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_created_at ON public.orders USING btree (created_at);


--
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- Name: idx_orders_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_orders_user_id ON public.orders USING btree (user_id);


--
-- Name: idx_products_brand_model_year; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_products_brand_model_year ON public.products USING btree (brand, model, year);


--
-- Name: idx_reclamations_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reclamations_created_at ON public.reclamations USING btree (created_at);


--
-- Name: idx_reclamations_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reclamations_status ON public.reclamations USING btree (status);


--
-- Name: idx_reclamations_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reclamations_user_id ON public.reclamations USING btree (user_id);


--
-- Name: idx_recommendations_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_recommendations_created_at ON public.recommendations USING btree (created_at);


--
-- Name: idx_recommendations_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_recommendations_user_id ON public.recommendations USING btree (user_id);


--
-- Name: idx_reports_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reports_created_at ON public.reports USING btree (created_at);


--
-- Name: idx_reports_report_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reports_report_type ON public.reports USING btree (report_type);


--
-- Name: idx_stock_alerts_product_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stock_alerts_product_id ON public.stock_alerts USING btree (product_id);


--
-- Name: idx_stock_movements_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stock_movements_date ON public.stock_movements USING btree (movement_date);


--
-- Name: idx_stock_movements_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_stock_movements_product ON public.stock_movements USING btree (product_id);


--
-- Name: idx_vehicles_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vehicles_user_id ON public.vehicles USING btree (user_id);


--
-- Name: supplier_products fk16e0uqyscot6pd4fqdw0uwlhf; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_products
    ADD CONSTRAINT fk16e0uqyscot6pd4fqdw0uwlhf FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);


--
-- Name: cart_items fk1re40cjegsfvw58xrkdp6bac6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fk1re40cjegsfvw58xrkdp6bac6 FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: orders fk32ql8ubntj5uh44ph9659tiih; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT fk32ql8ubntj5uh44ph9659tiih FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: recommendations fk3c9w1lipqdutm65a9inevwfp0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recommendations
    ADD CONSTRAINT fk3c9w1lipqdutm65a9inevwfp0 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: deliveries fk7isx0rnbgqr1dcofd5putl6jw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.deliveries
    ADD CONSTRAINT fk7isx0rnbgqr1dcofd5putl6jw FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: payments fk81gagumt0r8y3rmudcgpbk42l; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fk81gagumt0r8y3rmudcgpbk42l FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: supplier_products fk9hkf1oec6mv7lwo4ai3c0uv2c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supplier_products
    ADD CONSTRAINT fk9hkf1oec6mv7lwo4ai3c0uv2c FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: reorder_settings fkaiduvofvj2ygm9y0q8v248kop; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reorder_settings
    ADD CONSTRAINT fkaiduvofvj2ygm9y0q8v248kop FOREIGN KEY (preferred_supplier_id) REFERENCES public.suppliers(id);


--
-- Name: carts fkb5o626f86h46m4s7ms6ginnop; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.carts
    ADD CONSTRAINT fkb5o626f86h46m4s7ms6ginnop FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: order_items fkbioxgbv59vetrxe0ejfubep1w; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fkbioxgbv59vetrxe0ejfubep1w FOREIGN KEY (order_id) REFERENCES public.orders(id);


--
-- Name: payments fkj94hgy9v5fw1munb90tar2eje; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT fkj94hgy9v5fw1munb90tar2eje FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: stock_movements fkjcaag8ogfjxpwmqypi1wfdaog; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stock_movements
    ADD CONSTRAINT fkjcaag8ogfjxpwmqypi1wfdaog FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: reclamations fklequpexvy3mxq3re986vrw51r; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reclamations
    ADD CONSTRAINT fklequpexvy3mxq3re986vrw51r FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: reorder_settings fknileyvmqc2mdx7cht4wgrvx67; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reorder_settings
    ADD CONSTRAINT fknileyvmqc2mdx7cht4wgrvx67 FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: purchase_order_items fko3yj8ocbw2kav38548t22hgh8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_order_items
    ADD CONSTRAINT fko3yj8ocbw2kav38548t22hgh8 FOREIGN KEY (purchase_order_id) REFERENCES public.purchase_orders(id);


--
-- Name: vehicles fko4u5y92lt2sx8y2dc1bb9sewc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehicles
    ADD CONSTRAINT fko4u5y92lt2sx8y2dc1bb9sewc FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: order_items fkocimc7dtr037rh4ls4l95nlfi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.order_items
    ADD CONSTRAINT fkocimc7dtr037rh4ls4l95nlfi FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: users fkp56c1712k691lhsyewcssf40f; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fkp56c1712k691lhsyewcssf40f FOREIGN KEY (role_id) REFERENCES public.roles(id);


--
-- Name: cart_items fkpcttvuq4mxppo8sxggjtn5i2c; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cart_items
    ADD CONSTRAINT fkpcttvuq4mxppo8sxggjtn5i2c FOREIGN KEY (cart_id) REFERENCES public.carts(id);


--
-- Name: conversations fkpltqvfcbkql9svdqwh0hw4g1d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT fkpltqvfcbkql9svdqwh0hw4g1d FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: purchase_orders fkrpdasmb8y8xs5tiy4369xpinq; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_orders
    ADD CONSTRAINT fkrpdasmb8y8xs5tiy4369xpinq FOREIGN KEY (supplier_id) REFERENCES public.suppliers(id);


--
-- Name: purchase_order_items fks16e5vrvsp8alu0xp8m3a2ol5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.purchase_order_items
    ADD CONSTRAINT fks16e5vrvsp8alu0xp8m3a2ol5 FOREIGN KEY (product_id) REFERENCES public.products(id);


--
-- Name: messages fkt492th6wsovh1nush5yl5jj8e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkt492th6wsovh1nush5yl5jj8e FOREIGN KEY (conversation_id) REFERENCES public.conversations(id);


--
-- PostgreSQL database dump complete
--

\unrestrict jOgaxtR0ELMolnPhEK9LloeYkZVRp8mxffhOT8nbTSLaAbdjCJUxE78SlaYdTTW

