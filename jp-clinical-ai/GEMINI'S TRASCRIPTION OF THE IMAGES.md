**Thank you for uploading the first batch. We will process these exactly as you**

**requested, ensuring zero omissions, exact text matching, and careful flagging of**

**mandatory formatting and clinical judgment fields.**



**I have noted that there are 35 images in total and that they may be out of**

**order. We will compile the finalized, categorized JSON schema master list at the**

**very end once all 35 images are processed.**



**Here is the exhaustive extraction for Batch 1 (Images 1 - 4):**



**Image 1**



**Source Form: Unknown (Page header not visible, likely a Nursing/Assessment**

**continuation sheet) Section Name: Medication**



**1. Free Text Area:**



&#x20; **- Label: Medication (Bold)**

&#x20; **- Sub-label/Prompt: Regular medication, dosage, side effects, allergies,**

&#x20;   **compliance issues**

&#x20; **- Type: Free text box**

&#x20; **- Flags: Heading is bold (suggests mandatory section). Requires clinical**

&#x20;   **judgment (assessing side effects and compliance issues).**

&#x20; **- Proposed JSON Key: medication\_details**



**2. Functional Assessment Table:**



&#x20; **- Structure: Grid with 5 columns and 10 rows.**

&#x20; **- Column Headers:**

&#x20;   **1.  \[No Header / Blank] - Contains the task description**

&#x20;   **2.  Able**

&#x20;   **3.  Able with difficulty**

&#x20;   **4.  Able with equipment**

&#x20;   **5.  Not able**

&#x20; **- Row Labels (Task Descriptions in Column 1):**

&#x20;     **- Removing from packaging**

&#x20;     **- Reading labels**

&#x20;     **- Consent from individual to seek information**

&#x20;     **- Taking the right dose at the right time**

&#x20;     **- Swallowing tablets/liquids**

&#x20;     **- Using equipment aids (inhalers/oxygen)**

&#x20;     **- Storing your medication safely**

&#x20;     **- Disposing your medication safely**

&#x20;     **- Ordering your medication**

&#x20;     **- Collecting your medication**

&#x20; **- Cell Options: Each row requires a single selection/tick under one of the 4**

&#x20;   **ability columns.**

&#x20; **- Flags: Boxed formatting implies it is an unskippable mandatory assessment.**

&#x20;   **Assessing "Able with difficulty" requires clinical/functional judgment,**

&#x20;   **though it relies heavily on factual reporting.**

&#x20; **- Proposed JSON Keys:**

&#x20;     **- medication\_task\_removing\_from\_packaging**

&#x20;     **- medication\_task\_reading\_labels**

&#x20;     **- medication\_task\_consent\_seek\_info**

&#x20;     **- medication\_task\_taking\_right\_dose**

&#x20;     **- medication\_task\_swallowing**

&#x20;     **- medication\_task\_using\_equipment**

&#x20;     **- medication\_task\_storing\_safely**

&#x20;     **- medication\_task\_disposing\_safely**

&#x20;     **- medication\_task\_ordering**

&#x20;     **- medication\_task\_collecting**



**Image 2**



**Source Form: Belfast Trust Emergency Mental Health Assessment Proforma V4**



**Section: Patient Details**



&#x20; **- Structure: A multi-cell boxed grid.**

&#x20; **- Row 1:**

&#x20;     **- Name (Free text box) -> patient\_name**

&#x20;     **- H\&C (Free text box) -> health\_and\_care\_number**

&#x20; **- Row 2:**

&#x20;     **- Address (Free text box spanning down) -> patient\_address**

&#x20;     **- Referral Agent (Free text box) -> referral\_agent**

&#x20; **- Row 3:**

&#x20;     **- (Address continues left)**

&#x20;     **- Date (Free text box) -> assessment\_date (Flag: Date is bolded, implying**

&#x20;       **a strictly mandatory field)**

&#x20; **- Row 4:**

&#x20;     **- DOB (Free text box) -> patient\_dob**

&#x20;     **- Age (Free text box) -> patient\_age**

&#x20;     **- Time Referral Received at MHLT (Free text box) ->**

&#x20;       **time\_referral\_received\_mhlt**

&#x20; **- Row 5:**

&#x20;     **- Patient's Contact Number (Free text box spanning down) ->**

&#x20;       **patient\_contact\_number**

&#x20;     **- Time Assessment Started: (Free text box) -> time\_assessment\_started**

&#x20; **- Row 6:**

&#x20;     **- (Patient's Contact Number continues left)**

&#x20;     **- Time Assessment Finished: (Free text box) -> time\_assessment\_finished**

&#x20; **- Row 7:**

&#x20;     **- (Patient's Contact Number continues left)**

&#x20;     **- Assessor's Name (Free text box spanning down) -> assessor\_name**

&#x20; **- Row 8:**

&#x20;     **- Next of Kin (Free text box) -> next\_of\_kin\_name**

&#x20;     **- (Assessor's Name continues right)**

&#x20; **- Row 9:**

&#x20;     **- Next of Kin Contact Number (Free text box) -> next\_of\_kin\_contact\_number**

&#x20;     **- GP's Name (Free text box) -> gp\_name**

&#x20; **- Row 10:**

&#x20;     **- Place of Assessment (Free text box spanning down) -> place\_of\_assessment**

&#x20;     **- GP's Address (Free text box spanning down) -> gp\_address**

&#x20; **- Row 11:**

&#x20;     **- (Place of Assessment continues left)**

&#x20;     **- GP's Contact Number (Free text box) -> gp\_contact\_number**

&#x20; **- Flags: All fields are factual observations. The grid layout implies all**

&#x20;   **fields are expected/mandatory to complete.**



**Section: Other Professionals / Services Involved/Key Worker**



&#x20; **- Structure: Grid with 3 columns and 3 blank rows for free text entry.**

&#x20; **- Column Headers:**

&#x20;     **- Name -> other\_professional\_name\_1, \_2, \_3**

&#x20;     **- Service -> other\_professional\_service\_1, \_2, \_3**

&#x20;     **- Contact Number -> other\_professional\_contact\_number\_1, \_2, \_3**

&#x20; **- Flags: Factual observation. Boxed grid.**



**Section: Administrative / Consent Checkboxes**



&#x20; **- Structure: 5 standalone checkboxes with text labels printed in all-caps**

&#x20;   **bold.**

&#x20; **- Items:**

&#x20;     **- CONSIDERATION GIVEN TO PHYSICAL/SENSORY LIMITATIONS ☐ ->**

&#x20;       **consideration\_physical\_sensory\_limitations (Flag: Requires clinical**

&#x20;       **judgment)**

&#x20;     **- CONSENT OBTAINED ☐ -> consent\_obtained**

&#x20;     **- CONFIDENTIALITY DISCUSSED ☐ -> confidentiality\_discussed**

&#x20;     **- ZERO TOLERANCE DISCUSSED IF REQUIRED ☐ -> zero\_tolerance\_discussed**

&#x20;     **- CONSENT OBTAINED TO SPEAK TO CAREGIVER/MAIN SUPPORT ☐ ->**

&#x20;       **consent\_speak\_caregiver**

&#x20; **- Flags: All text is bolded. This indicates mandatory legal/administrative**

&#x20;   **steps that must be completed.**



**Section: Clinical information from referrer**



&#x20; **- Label: Clinical information from referrer (Bold)**

&#x20; **- Type: Free text box.**

&#x20; **- Flags: Bold heading implies a mandatory section. Factual extraction of**

&#x20;   **third-party clinical information.**

&#x20; **- Proposed JSON Key: clinical\_information\_referrer**



**Image 3**



**Source Form: Unknown (Likely Assessment Proforma V4 continued)**



**Note on obscured text: On the top right edge of the page, outside the main**

**boxes, the text "...sical Hea" is partially visible. This is likely a cutoff**

**heading for "Physical Health" from an adjacent page in the scan/photocopy.**



**1. Mental Health Family History**



&#x20; **- Label: Mental Health Family History (Bold)**

&#x20; **- Sub-label/Prompt 1: Living arrangements, family dynamics, place of**

&#x20;   **residence, condition of accommodation, ability to manage, pets, response to**

&#x20;   **care/treatment, number of carers, caring for others, explore access to**

&#x20;   **lethal means, F+CC**

&#x20; **- Sub-label/Prompt 2: Parents, Siblings, Family History suicide, addictions,**

&#x20;   **mental illness.**

&#x20; **- Type: Free text box**

&#x20; **- Flags: Bold heading implies mandatory. Exploring "family dynamics" and**

&#x20;   **"ability to manage" requires clinical judgment.**

&#x20; **- Proposed JSON Key: mental\_health\_family\_history**



**2. Mental Health Abuse**



&#x20; **- Label: Mental Health Abuse (Bold)**

&#x20; **- Sub-label/Prompt: Any issues of abuse/vulnerability**

&#x20; **- Type: Free text box**

&#x20; **- Flags: Bold heading implies mandatory. Assessing "vulnerability" requires**

&#x20;   **clinical judgment.**

&#x20; **- Proposed JSON Key: mental\_health\_abuse**



**3. Mental Health Offending History**



&#x20; **- Label: Mental Health Offending History (Bold)**

&#x20; **- Sub-label/Prompt: Forensic history, current, pending, custodial, access to**

&#x20;   **weapons, gun license**

&#x20; **- Type: Free text box**

&#x20; **- Flags: Bold heading implies mandatory. Mostly factual observation/history**

&#x20;   **taking.**

&#x20; **- Proposed JSON Key: mental\_health\_offending\_history**



**4. Mental Health Social Circumstances**



&#x20; **- Label: Mental Health Social Circumstances (Bold)**

&#x20; **- Sub-label/Prompt: Housing, finance, debts, relationship, friendship,**

&#x20;   **support, socialising, hobbies, strengths**

&#x20; **- Type: Free text box**

&#x20; **- Flags: Bold heading implies mandatory. Identifying "strengths" requires**

&#x20;   **clinical judgment and formulation.**

&#x20; **- Proposed JSON Key: mental\_health\_social\_circumstances**



**Image 4**



**Source Form: Unknown (Likely Assessment Proforma V4 continued)**



**1. History of presenting complaint continued**



&#x20; **- Label: History of presenting complaint continued (Bold and underlined)**

&#x20; **- Type: Free text box spanning most of the page.**

&#x20; **- Flags: Bold and underlined formatting implies mandatory continuation.**

&#x20;   **Narrative relies heavily on clinical judgment to synthesize and formulate**

&#x20;   **the patient's presentation.**

&#x20; **- Proposed JSON Key: history\_of\_presenting\_complaint\_continued**



**2. Presenting Suicide Events (48 hrs)**



&#x20; **- Label: Presenting Suicide Events (48 hrs) (Bold and italicized)**

&#x20; **- Type: Free text area (unboxed, open space below the text).**

&#x20; **- Flags: Bold and italicized formatting implies mandatory section. High**

&#x20;   **clinical judgment required to evaluate suicidal events and intent.**

&#x20; **- Proposed JSON Key: presenting\_suicide\_events\_48\_hrs**



**Please go ahead and upload the next batch of 3 or 4 images whenever you are**

**ready!**




**Thank you for the second batch. I have carefully analyzed these 4 images (3**

**paper forms and 1 Epic EHR screenshot) following your exact instructions.**



**Here is the exhaustive extraction for Batch 2 (Images 5 - 8):**



**Image 5**



**Source Form: Unknown (Continuation of Clinical Assessment)**



**Section: History**



&#x20; **- Label: History (Bold and underlined)**

&#x20; **- Flags: Bold and underlined implies a mandatory top-level section.**



**Subsection 1: Mental Health History**



&#x20; **- Label: Mental Health History (Bold)**

&#x20; **- Sub-label/Prompt: Diagnosis, previous contact with service, details of**

&#x20;   **admissions, treatment to date, and use of MHO, details of previous self-harm**

&#x20; **- Type: Free text area within a large box.**

&#x20; **- Sub-field A: Recent suicide events over the last 2 months (Italicized text**

&#x20;   **with free text space below). ->**

&#x20;   **mental\_health\_history\_recent\_suicide\_events\_last\_2\_months**

&#x20; **- Sub-field B: Recent suicide events from before 2 months (Italicized text**

&#x20;   **with free text space below). ->**

&#x20;   **mental\_health\_history\_recent\_suicide\_events\_before\_2\_months**

&#x20; **- Flags: Bold heading indicates a mandatory section. Requires clinical**

&#x20;   **judgment to synthesize diagnosis, history, and evaluate the severity/intent**

&#x20;   **of past suicide events.**

&#x20; **- Proposed JSON Key: mental\_health\_history\_general**



**Subsection 2: Mental Health Personal History**



&#x20; **- Label: Mental Health Personal History (Bold)**

&#x20; **- Sub-label/Prompt: Early Childhood, Milestones, schooling, psycho sexual**

&#x20;   **history e.g sexual orientation, bullying, behavioural issues, significant**

&#x20;   **life events, trauma, abuse, relationships, employment, spiritual needs,**

&#x20;   **cultural needs, aspiration**

&#x20; **- Type: Free text area within a large box.**

&#x20; **- Flags: Bold heading indicates mandatory. Mostly factual historical data, but**

&#x20;   **assessing trauma and developmental milestones requires clinical skill.**

&#x20; **- Proposed JSON Key: mental\_health\_personal\_history**



**Image 6**



**Source Form: Unknown (Continuation of Clinical Assessment)**



**Section: History of presenting complaint**



&#x20; **- Label: History of presenting complaint (Bold and underlined)**

&#x20; **- Sub-label/Prompt: Reason for presentation, precipitating factors, course and**

&#x20;   **duration of problem, features of mental illness, management to date**

&#x20; **- Type: Large free text box spanning the entire page.**

&#x20; **- Flags: Bold and underlined formatting implies a highly mandatory, primary**

&#x20;   **section of the assessment. High clinical judgment required to formulate**

&#x20;   **precipitating factors and identify specific "features of mental illness."**

&#x20; **- Proposed JSON Key: history\_of\_presenting\_complaint\_initial**



**Image 7**



**Source Form: Unknown (Consent \& Collateral Information)**



**Note on obscured text: On the top right edge, text is cut off: "History o...",**

**"ason for prese...", "anagement to...". This is clearly bleed-over from the**

**adjacent "History of presenting complaint" page in the physical chart.**



**Section: Confidentiality**



&#x20; **- Label: Confidentiality (Bold and underlined)**

&#x20; **- Structure: Grid with 4 columns and 8 rows.**

&#x20; **- Column Headers:**

&#x20;   **1.  \[Blank / Task Description]**

&#x20;   **2.  Yes**

&#x20;   **3.  No**

&#x20;   **4.  Other**

&#x20; **- Row Labels \& Types:**

&#x20;     **- Row 1: Confidentiality Explained (Requires single tick in Yes/No/Other)**

&#x20;       **-> confidentiality\_explained**

&#x20;     **- Row 2: Patient has capacity to consent (Requires single tick in**

&#x20;       **Yes/No/Other) -> patient\_has\_capacity\_to\_consent (Flag: Assessing**

&#x20;       **capacity requires strict clinical/legal judgment)**

&#x20;     **- Row 3: Consent from individual to seek information (Requires single tick**

&#x20;       **in Yes/No/Other) -> consent\_individual\_seek\_information**

&#x20;     **- Row 4: Consent from individual to share information (Requires single**

&#x20;       **tick in Yes/No/Other) -> consent\_individual\_share\_information**

&#x20;     **- Row 5: Who can information be shared with (No tick boxes; the cell spans**

&#x20;       **the Yes/No/Other columns forming a wide free text box) ->**

&#x20;       **who\_information\_shared\_with**

&#x20;     **- Row 6: Consent from family or carer to share information (Requires**

&#x20;       **single tick in Yes/No/Other) -> consent\_family\_carer\_share\_information**

&#x20;     **- Row 7: Consent to contact patient by phone or other means (Requires**

&#x20;       **single tick in Yes/No/Other) -> consent\_contact\_patient\_phone\_other**

&#x20;     **- Row 8: Restrictions on third party information (No tick boxes; the cell**

&#x20;       **spans the Yes/No/Other columns forming a wide free text box) ->**

&#x20;       **restrictions\_third\_party\_information**

&#x20; **- Flags: Boxed grid and bold/underlined header imply this is a mandatory**

&#x20;   **medico-legal section.**



**Section: Collateral**



&#x20; **- Label: Collateral (Bold and underlined)**

&#x20; **- Fields (within a box):**

&#x20;     **- Provided by: (Free text area beside it) -> collateral\_provided\_by\_type**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ (Pre-printed line for text) -> collateral\_name**

&#x20;     **- Relationship: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ (Pre-printed line for text) ->**

&#x20;       **collateral\_relationship**

&#x20; **- Flags: Factual observations.**



**Image 8**



**Source Form: Epic EHR System Screenshot (Note: Because this is an EHR, it**

**combines system UI elements with clinical text. I have extracted both.)**



**1. System / Patient Context Header (Top Bar)**



&#x20; **- Visible text: "MENT TEAM - Production - JEANPIERRE KAGAME"**

&#x20; **- Patient Name Tab: BYRNE, Patrick G (Includes an 'X' to close tab)**

&#x20; **- Top Menu Buttons: Find Patients, Telephone Call, Encounter, Remind Me,**

&#x20;   **Document Episode Group, Search (Ctrl+Space)**



**2. EHR Navigation Tabs (Row 1)**



&#x20; **- Chart Re..., Summary, Notes, O Orders (Red circle with 'O'), Flowsheets,**

&#x20;   **Review F..., Screenings, YIM, Treatmen..., MH Meds, Restricte...**



**3. EHR Navigation Tabs (Row 2 - Under 'Notes')**



&#x20; **- Notes (Active tab), Progress, Cons WR, Consultations, Procedures, Hist \&**

&#x20;   **Exam, Discharge, Emergency, ACP, Care Plan, Periop, Events, M...**



**4. Note Meta-Data / Action Bar**



&#x20; **- Text: "Number of notes shown: 5 out of 5. More to load. Load all notes."**

&#x20; **- Text: "There are new updates. Sort by new notes. Mark all as not new."**

&#x20; **- Buttons: Addendum, Copy, Cosign, Attest, Sign, Route, Remove Cosign, Tag.**



**5. Main Clinical Note Content (Pre-filled / Draft Text)**



&#x20; **- Interventions List (Bulleted/newline list of factual actions):**



&#x20;     **- cognitive behavioural techniques**

&#x20;     **- goal setting**

&#x20;     **- health promotion education**

&#x20;     **- medication concordance monitoring**

&#x20;     **- medication education**

&#x20;     **- mental state monitoring**

&#x20;     **- problem solving approach**

&#x20;     **- psychosocial interventions**

&#x20;     **- re-framing thoughts**

&#x20;     **- recovering well session**

&#x20;     **- relapse prevention**

&#x20;     **- risk assessing**

&#x20;     **- risk management**

&#x20;     **- sleep hygiene**

&#x20;     **- social activity**

&#x20;     **- solution focused approach undertaken**

&#x20;     **- support and reassurance provided**

&#x20;     **- symptom management**

&#x20;     **- symptom recognition**

&#x20;     **- Proposed JSON Key (Array of strings): epic\_interventions\_provided**



&#x20; **- Risk Section:**



&#x20;     **- Label: Risk: (Bold)**

&#x20;     **- Pre-filled text:**

&#x20;         **- "No TLNWL / No S.I plan to hang"**

&#x20;         **- "No TSH reported"**

&#x20;         **- "TLNWL / S.I plan to hang or alcohol poisoning"**

&#x20;         **- "No TSH reported"**

&#x20;         **- "Her family are protective factor"**

&#x20;     **- Obscured text note: The sentence cuts off slightly on the right side of**

&#x20;       **the monitor.**

&#x20;     **- Abbreviations identified: TLNWL (Thoughts of Life Not Worth Living), S.I**

&#x20;       **(Suicidal Ideation), TSH (Thoughts of Self Harm).**

&#x20;     **- Flags: Requires very high clinical judgment. Note: There are**

&#x20;       **contradictions in this pre-filled text (e.g., saying "No TLNWL" then**

&#x20;       **later "TLNWL", and using "Her" for a patient named Patrick). This**

&#x20;       **suggests it is a draft template not yet fully edited by the clinician.**

&#x20;     **- Proposed JSON Key: epic\_risk\_assessment\_text**



&#x20; **- Plan Section:**



&#x20;     **- Label: Plan: (Bold)**

&#x20;     **- Pre-filled text:**

&#x20;         **- "Medics to r/v meds" (Bolded text)**

&#x20;         **- "Ongoing support"**

&#x20;         **- "He is aware how contact trust safety procedures ie ED, HTT, out of**

&#x20;           **hrs GP, as well as charity helpline ie lifeline and Samaritans,**

&#x20;           **whenever he fee\[ls]..." (Text cuts off at edge of screen: 'thoughts**

&#x20;           **etc,')**

&#x20;         **- "He knows he should be concordant with his meds and liaise with HTT**

&#x20;           **/ GP. Out of hrs GP is he experiences any adverse side effects."**

&#x20;     **- Abbreviations identified: r/v (review), meds (medications), ED**

&#x20;       **(Emergency Department), HTT (Home Treatment Team), hrs (hours), GP**

&#x20;       **(General Practitioner).**

&#x20;     **- Proposed JSON Key: epic\_plan\_text**



&#x20; **- Bottom Taskbar (System UI):**



&#x20;     **- Chat | General |...**

&#x20;     **- HE DV notes updat...**

&#x20;     **- Hyperspace - BT H...**

&#x20;     **- Sticky note on physical monitor reads: "214 869 Computer No"**



**Please go ahead and upload the next batch of images when you are ready!**





**Thank you for the third batch. I have carefully analyzed these 4 images (1 Epic**

**EHR screenshot and 3 paper forms) following your exact instructions.**



**Here is the exhaustive extraction for Batch 3 (Images 9 - 12):**



**Image 9**



**Source Form: Epic EHR System Screenshot (Clinical Note)**



**System / Note Header**



&#x20; **- Visible Text/Tabs: "Mental Health", "Signed"**



**Section 1: Type and Purpose**



&#x20; **- Label: Type and Purpose of the contact: (Bold)**

&#x20; **- Pre-filled/Draft Text: "Joint base r/v with Paula H SSW to assess his mental**

&#x20;   **state, mood, meds compliance, and adverse side effects and risks if any."**

&#x20; **- Abbreviations: r/v (review), SSW (Senior Social Worker - inferred), meds**

&#x20;   **(medications).**

&#x20; **- Proposed JSON Key: epic\_type\_and\_purpose\_of\_contact**



**Section 2: Narrative Overview**



&#x20; **- Label: Contact details/general overview: (Bold)**

&#x20; **- Pre-filled/Draft Text: (Transcribed entirely to capture narrative style) "He**

&#x20;   **arrived late for his r/v and apologised to staff, it was staff first time of**

&#x20;   **meeting him and therefore, formal introduction was made. His first word was**

&#x20;   **he's isn't feeling great and in fact he hasn't been feeling great for the**

&#x20;   **past 4 weeks, he stated, he feels the trauma he encountered 25 yrs ago feels**

&#x20;   **like it happened couple of says ago, for some reason he wanted to validate**

&#x20;   **if I've read his notes before, which I reply indeed I've read his notes**

&#x20;   **prior to his r/v. he went on and said he found it very difficult today to do**

&#x20;   **his shoelaces where he was coming here; I asked him if he feels his meds are**

&#x20;   **effective which he replied no he has been on them for 4 weeks but there's no**

&#x20;   **improving with his flash backs, he was very descriptive of his ordeal**

&#x20;   **previous and present, the feeling of confusion, out of world experience, his**

&#x20;   **veins pops up, he doesn't even recognised his own wife and daughters,**

&#x20;   **neither his home environment. I asked him what does think we can extra to**

&#x20;   **help him, he dodged the question I revisited and asked him he went around it**

&#x20;   **instead of giving a direct answer in the end, he said there isn't anything**

&#x20;   **else, he likes coming down to base r/v because it gives him a purpose. My**

&#x20;   **impression was he didn't want to be discharge from our caseload as yet due**

&#x20;   **to the way he is going by answering and revisiting his trauma experience to**

&#x20;   **staff. It seems he was asking for hospital admission at some stage in**

&#x20;   **directly, by saying he like the idea of surrounded by professionals staff**

&#x20;   **advised him that will be counterproductive due to his current mental state**

&#x20;   **which isn't acute rather than chronic experiences, I asked him what is his**

&#x20;   **protective factor he said it's his family, and he loves one of his**

&#x20;   **granddaughter and will do anything for her. He also said his wife isn't well**

&#x20;   **physically and he's the one who should be looking after her, not the other**

&#x20;   **way round. He spoke about handing his guns to PSNI 12 years ago, nil plans**

&#x20;   **or intent expressed. He described his mood today as 1 out of and usually his**

&#x20;   **mood sits around 5 out of 10, he's very appreciative about HTT staff because**

&#x20;   **each time he meets a new staff he learns something new from them which helps**

&#x20;   **hm immensely, he's future orientated. Nil overt depressive nor psychotic**

&#x20;   **symptoms evident. Reassurance given with good effect. I have liaise with one**

&#x20;   **of his medics and he's happy to r/v his meds asap with the hope to DC to**

&#x20;   **PMHT. He appears well, fit, nourished and hydrated, reassurance given with**

&#x20;   **good effect."**

&#x20; **- Abbreviations Identified: r/v (review), PSNI (Police Service of Northern**

&#x20;   **Ireland), HTT (Home Treatment Team), DC (Discharge), PMHT (Primary Mental**

&#x20;   **Health Team).**

&#x20; **- Flags: Pure narrative format requiring high clinical judgment to formulate**

&#x20;   **impression (e.g., "My impression was he didn't want to be discharge",**

&#x20;   **"asking for hospital admission... in directly").**

&#x20; **- Proposed JSON Key: epic\_contact\_details\_general\_overview**



**Section 3: Mental State Examination (EHR)**



&#x20; **- Label: Mental state examination: (Bold)**

&#x20; **- Sub-sections:**

&#x20;     **- Appearance and Behaviour- (Bold)**

&#x20;         **- Text: "Dressed in sports gear top and bottom, and trainers**

&#x20;           **appropriate for indoor / outdoor weather. He elicited good eye**

&#x20;           **contact. Nil labile mood evident. He appears kempt and groomed. He**

&#x20;           **appears relaxed, and calm" -> epic\_mse\_appearance\_behaviour**

&#x20;     **- Speech / Thought Form- (Bold)**

&#x20;         **- Text: "Normal, tone, volume rate, coherence, spontaneous and**

&#x20;           **euthymic" -> epic\_mse\_speech\_thought\_form**

&#x20;     **- Mood and Affect- (Bold)**

&#x20;         **- Text: "Both subj obj appears remains stable nil labile mood evident.**

&#x20;           **Mood appears bright and reactive." -> epic\_mse\_mood\_affect**

&#x20;     **- Suicidality - (Bold)**

&#x20;         **- (Text cuts off at the bottom of the screen) -> epic\_mse\_suicidality**



**Image 10**



**Source Form: Leeds Dependence Questionnaire – LDQ (Paper Form)**



**Form Header**



&#x20; **- Title: Leeds Dependence Questionnaire – LDQ (Bold)**

&#x20; **- Instructions: Here are some questions about the importance of alcohol or**

&#x20;   **other drugs in your life. Think about the main substance you have been using**

&#x20;   **over the last 4 weeks and tick the closest answer to how you see yourself**

&#x20;   **(Italicized)**



**Questionnaire Table Structure**



&#x20; **- Grid with 10 questions and 4 scoring checkbox columns.**

&#x20; **- Column Headers:**

&#x20;     **- Never 0**

&#x20;     **- Sometimes 1**

&#x20;     **- Often 2**

&#x20;     **- Nearly Always 3**



**Questions (Each requires a checkbox selection):**



**1.  Do you find yourself thinking about when you will next be able to have**

&#x20;   **another drink or take more drugs? -> ldq\_q1\_thinking\_next**

**2.  Is drinking or taking drugs more important than anything else you might do**

&#x20;   **during the day? -> ldq\_q2\_more\_important**

**3.  Do you feel that your need for drink or drugs is too strong to control? ->**

&#x20;   **ldq\_q3\_too\_strong**

**4.  Do you plan your days around getting and taking drink or drugs? ->**

&#x20;   **ldq\_q4\_plan\_days**

**5.  Do you drink or take drugs in a particular way in order to increase the**

&#x20;   **effect it gives you? -> ldq\_q5\_increase\_effect**

**6.  Do you drink or take drugs morning, afternoon and evening? ->**

&#x20;   **ldq\_q6\_morning\_afternoon\_evening**

**7.  Do you feel you have to carry on drinking or taking drugs once, you have**

&#x20;   **started? -> ldq\_q7\_carry\_on**

**8.  Is getting an effect more important than the particular drink or drug you**

&#x20;   **use? -> ldq\_q8\_effect\_more\_important**

**9.  Do you want to take more drink or drugs when the effects start to wear off?**

&#x20;   **-> ldq\_q9\_take\_more\_wearing\_off**

**10. Do you find it difficult to cope with life without drink or drugs? ->**

&#x20;   **ldq\_q10\_difficult\_cope**



**Scoring \& Interpretation**



&#x20; **- Total Box: Total LDQ Score = \[Blank Box] -> ldq\_total\_score**

&#x20; **- Guidance Text 1: Considering the score and if the patient has any complex**

&#x20;   **issues, if patient has a score of 20 or more consider onward referral to**

&#x20;   **CAT.**

&#x20; **- Guidance Text 2: If below this score, consider referral to voluntary**

&#x20;   **services, for example, Addictions NI, Dunlewy, or Daisy if under the age**

&#x20;   **of 25.**

&#x20; **- Cut-off Guide:**

&#x20;     **- <10 = low dependence;**

&#x20;     **- 10-22 = medium dependence; and**

&#x20;     **- 22 = high dependence.**

&#x20; **- Flags: This is a standardized, strictly mandatory structured assessment.**

&#x20;   **While the answers are factual self-reports, the clinical guidance at the**

&#x20;   **bottom mandates clinical judgment regarding onward referral pathways based**

&#x20;   **on the final score.**



**Image 11**



**Source Form: Safety Plan (Paper Form)**



**Structure \& Layout**



&#x20; **- Table format with 6 shaded row headers acting as steps.**



**Step 1**



&#x20; **- Header: Step 1: Warning signs (thoughts, images, mood, situation, behaviour)**

&#x20;   **that a crisis may be developing: (Bold, Shaded)**

&#x20; **- Fields: Lines numbered 1, 2, 3. -> safety\_plan\_step1\_1, safety\_plan\_step1\_2,**

&#x20;   **safety\_plan\_step1\_3**



**Step 2**



&#x20; **- Header: Step 2: Internal coping strategies – Things I can do to take my mind**

&#x20;   **off my problems without contacting another person (relaxation technique,**

&#x20;   **physical activity): (Bold, Shaded)**

&#x20; **- Fields: Lines numbered 1, 2, 3. -> safety\_plan\_step2\_1, safety\_plan\_step2\_2,**

&#x20;   **safety\_plan\_step2\_3**



**Step 3**



&#x20; **- Header: Step 3: People and social settings that provide distraction: (Bold,**

&#x20;   **Shaded)**

&#x20; **- Fields:**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Place: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step3\_name\_1,**

&#x20;       **safety\_plan\_step3\_place\_1**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Place: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step3\_name\_2,**

&#x20;       **safety\_plan\_step3\_place\_2**



**Step 4**



&#x20; **- Header: Step 4: People whom I can ask for help (Supporters): (Bold,**

&#x20;   **Italicized 'Supporters', Shaded)**

&#x20; **- Fields:**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Phone: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step4\_name\_1,**

&#x20;       **safety\_plan\_step4\_phone\_1**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Phone: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step4\_name\_2,**

&#x20;       **safety\_plan\_step4\_phone\_2**

&#x20;     **- Name: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Phone: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step4\_name\_3,**

&#x20;       **safety\_plan\_step4\_phone\_3**



**Step 5**



&#x20; **- Header: Step 5: Professionals or agencies I can contact during a crisis:**

&#x20;   **(Bold, Shaded)**

&#x20; **- Fields:**

&#x20;     **- 1.  Professional : \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Phone: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ ->**

&#x20;           **safety\_plan\_step5\_professional\_1, safety\_plan\_step5\_phone\_1**

&#x20;     **- 2.  GP: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ Phone: \_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> safety\_plan\_step5\_gp,**

&#x20;           **safety\_plan\_step5\_gp\_phone**

&#x20;     **- 3.  GP OOH: 02890744447 (BelDOC) 02890796220 (SEBDOC) (Pre-printed**

&#x20;           **numbers)**

&#x20;     **- 4.  Lifeline (24/7) Helpline Phone number: 0808 808 8000. (Pre-printed**

&#x20;           **numbers)**

&#x20; **- Pre-printed Subtext: (Deaf and hard of hearing users can access Lifeline**

&#x20;   **through the SignVideo App available on smartphones by downloading through**

&#x20;   **the app store. Or via text phone on 18001 0808 808 8000. Calls to Lifeline**

&#x20;   **are free to people living in Northern Ireland who are calling from UK**

&#x20;   **landlines and mobiles)**



**Step 6**



&#x20; **- Header: Step 6: Making the environment safe: (Bold, Shaded)**

&#x20; **- Fields:**

&#x20;     **- Lines numbered 1, 2. -> safety\_plan\_step6\_1, safety\_plan\_step6\_2**

&#x20;     **- The one thing that is most important to me and worth living for is:**

&#x20;       **(Bold prompt with blank line) -> safety\_plan\_most\_important\_thing**

&#x20;     **- I agree to a Follow-up call YES / NO. If yes, date of follow-up call:**

&#x20;       **(Bold prompt with blank line) -> safety\_plan\_follow\_up\_call\_consent,**

&#x20;       **safety\_plan\_follow\_up\_call\_date**

&#x20; **- Flags: Safety Plans are collaborative but require significant clinical**

&#x20;   **judgment to ensure interventions listed are realistic and safe. The shaded**

&#x20;   **blocks imply this is a mandatory, structured document.**



**Image 12**



**Source Form: Mental State Examination (Paper Form)**



**Note on obscured text: The top right corner has cutoff text: "ought Content",**

**"occupations, ob...", "thdrawal/inserti...", "eas of referen...". This is bleed**

**over from the subsequent "Thought Content" page.**



**Section Heading: Mental State Examination (Bold and underlined)**



&#x20; **- Flags: Formatting implies a mandatory, primary clinical document.**



**Domain 1: Appearance and Behaviour**



&#x20; **- Label: Appearance and Behaviour (Bold)**

&#x20; **- Prompt: Clothes, self-care, motor activity, rapport, eye-contact**

&#x20;   **(Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Proposed JSON Key: mse\_appearance\_behaviour**



**Domain 2: Speech / Thought Form**



&#x20; **- Label: Speech / Thought Form (Bold)**

&#x20; **- Prompt: Spontaneity, coherence, rate, tone, volume, thought disorder**

&#x20;   **(Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Proposed JSON Key: mse\_speech\_thought\_form**



**Domain 3: Mood and Affect**



&#x20; **- Label: Mood and Affect (Bold)**

&#x20; **- Prompt: Subjectively + objectively, fluctuation in mood, depressed/not**

&#x20;   **depressed/euthymic/elated, anxiety / panic, reactivity (Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Proposed JSON Key: mse\_mood\_affect**



**Domain 4: Suicidality**



&#x20; **- Label: Suicidality (Bold)**

&#x20; **- Prompt 1: TLNWL, Suicidal ideation, Suicidal plan or intent, TSH, Self-harm**

&#x20;   **plan or intent (Italicized)**

&#x20; **- Prompt 2 (Question): Has there been a suicide attempt on this presentation?**

&#x20;   **Yes No (Implies a mandatory circle/tick) ->**

&#x20;   **mse\_suicide\_attempt\_this\_presentation**

&#x20; **- Prompt 3 (Warning/Instruction): Has advice been provided surrounding this**

&#x20;   **attempt and the risk of death if re attempted? I.e. no safe limits with**

&#x20;   **self-poisoning, jumping from a height, attempted hanging (Bold and**

&#x20;   **Italicized)**

&#x20; **- Type: Free text box to write out the details of suicidality and the advice**

&#x20;   **given. -> mse\_suicidality\_details**

&#x20; **- Prompt 4 (Bottom of box): Do you have any access to weapons or have a gun**

&#x20;   **license (Italicized) -> mse\_access\_weapons\_guns**

&#x20; **- Flags: This entire domain requires paramount clinical judgment. The bolding,**

&#x20;   **italicizing, and explicit questions about specific suicide methods (hanging,**

&#x20;   **poisoning) strongly flag this as the most mandatory risk-assessment section**

&#x20;   **on the page.**



**Please go ahead and upload the next batch of images whenever you are ready!**







**Thank you for the fourth batch. I have carefully extracted all details from**

**these 4 paper forms, applying your strict rules for formatting, clinical**

**judgment flags, and zero omissions.**



**Here is the exhaustive extraction for Batch 4 (Images 13 - 16):**



**Image 13**



**Source Form: Mental State Examination (Paper Form, Continuation from Image 12)**



**Domain 5: Thought Content**



&#x20; **- Label: Thought Content (Bold)**

&#x20; **- Prompt: Preoccupations, obsessions, delusions, paranoid thoughts, thought**

&#x20;   **interference /- withdrawal/insertion/broadcast/echo, passivity phenomena,**

&#x20;   **and violent thoughts/thoughts to harm others, ideas of reference**

&#x20;   **(Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Flags: Bold heading implies mandatory domain. Extremely high clinical**

&#x20;   **judgment required to assess and describe delusions, passivity phenomena, and**

&#x20;   **thought interference.**

&#x20; **- Proposed JSON Key: mse\_thought\_content**



**Domain 6: Perceptual disturbances in all modalities**



&#x20; **- Label: Perceptual disturbances in all modalities (Bold)**

&#x20; **- Prompt: Auditory, visual, gustatory, olfactory, tactile (Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Flags: Bold heading implies mandatory. Requires clinical judgment to**

&#x20;   **distinguish true hallucinations from illusions or internal monologues.**

&#x20; **- Proposed JSON Key: mse\_perceptual\_disturbances**



**Domain 7: Cognition**



&#x20; **- Label: Cognition (Bold)**

&#x20; **- Prompt: Orientation, attention, memory– consider MMSE (Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Flags: Bold heading implies mandatory. Assesses facts (orientation) but**

&#x20;   **requires clinical judgment to evaluate attention and memory deficits.**

&#x20; **- Proposed JSON Key: mse\_cognition**



**Domain 8: Insight**



&#x20; **- Label: Insight (Bold)**

&#x20; **- Prompt: Awareness of illness and need for help, willingness to engage**

&#x20;   **(Italicized)**

&#x20; **- Type: Free text box.**

&#x20; **- Flags: Bold heading implies mandatory. This is a pure clinical judgment**

&#x20;   **metric (assessing the patient's internal understanding of their illness).**

&#x20; **- Proposed JSON Key: mse\_insight**



**Image 14**



**Source Form: PISANI (Risk Assessment Framework)**



**Section Heading: PISANI (Bold and underlined)**



&#x20; **- Flags: PISANI (Preventing Incidents of Suicide and Non-fatal Intentional**

&#x20;   **self-harm) is a specific risk formulation model. The formatting indicates a**

&#x20;   **mandatory, highly critical clinical risk document.**



**Subsections (All are free text boxes running sequentially down the page):**



**1.  Strengths \& Protective Factors (Bold)**

&#x20;     **- Flags: Requires clinical judgment to formulate what acts as a protective**

&#x20;       **buffer for the patient. -> pisani\_strengths\_protective\_factors**

**2.  Long Term Risk Factors (Bold)**

&#x20;     **- Flags: Requires clinical judgment to synthesize historical context. ->**

&#x20;       **pisani\_long\_term\_risk\_factors**

**3.  Impulsivity/Self control (Bold)**

&#x20;     **- Flags: Requires high clinical judgment to assess trait/state**

&#x20;       **impulsivity. -> pisani\_impulsivity\_self\_control**

**4.  Past Suicidal Behaviours (Bold)**

&#x20;     **- Flags: Primarily factual history gathering. ->**

&#x20;       **pisani\_past\_suicidal\_behaviours**

**5.  Recent/Present Suicidal Behaviours (Bold)**

&#x20;     **- Flags: Requires clinical judgment to assess severity and intent. ->**

&#x20;       **pisani\_recent\_present\_suicidal\_behaviours**

**6.  Stressors/Precipitants (Bold)**

&#x20;     **- Flags: Requires clinical judgment to identify specific triggers. ->**

&#x20;       **pisani\_stressors\_precipitants**

**7.  Symptoms, Suffering and Recent Changes (Bold)**

&#x20;     **- Flags: Clinical synthesis of presentation. ->**

&#x20;       **pisani\_symptoms\_suffering\_recent\_changes**

**8.  Engagement and Reliability (Bold)**

&#x20;     **- Flags: Pure clinical judgment evaluating the therapeutic alliance and**

&#x20;       **patient honesty. -> pisani\_engagement\_and\_reliability**



**Image 15**



**Source Form: Child Protection Details (Paper Form)**



**Section Heading: Child Protection Details (Bold)**



&#x20; **- Flags: This entire page is heavily boxed and bordered, indicating a strict,**

&#x20;   **unskippable legal/safeguarding requirement.**



**Demographics Block:**



&#x20; **- Patient's Name (Bold, Free text box) -> child\_protection\_patient\_name**

&#x20; **- Marital Status: Married / Separated / Divorced / Single / Widowed (delete as**

&#x20;   **appropriate) (Bold label, requires striking out text) ->**

&#x20;   **child\_protection\_marital\_status**

&#x20; **- Partner's name (Bold, Free text box) -> child\_protection\_partner\_name**

&#x20; **- Partner's Address (Bold, Free text box) -> child\_protection\_partner\_address**



**Children Contact Table:**



&#x20; **- Structure: Grid with 7 columns and 5 blank rows for data entry.**

&#x20; **- Column Headers:**

&#x20;   **1.  Names of children with whom patient has regular contact (Bold)**

&#x20;   **2.  Sex (Bold)**

&#x20;   **3.  D.O.B (Bold)**

&#x20;   **4.  Relationship to patient (Bold)**

&#x20;   **5.  Relationship to partner (Bold)**

&#x20;   **6.  Name of mother/father if they are not living with child (Bold)**

&#x20;   **7.  Do they have contact with the child? (Bold)**

&#x20; **- Proposed JSON Key: child\_protection\_children\_contact\_list (Array of objects)**



**Safeguarding / Social Services Block 1:**



&#x20; **- Are Family \& Child Care (F\&CC) Social Services involved with the family?**

&#x20;   **(Free text area containing "Yes" and "No" spaces for ticking) ->**

&#x20;   **child\_protection\_fcc\_involved\_yes\_no**

&#x20; **- If yes, name of staff involved \& contact details (when last involved -**

&#x20;   **reasons for involvement / reasons for discharge) (Free text area) ->**

&#x20;   **child\_protection\_fcc\_involvement\_details**



**Safeguarding / Social Services Block 2:**



&#x20; **- For families not known to F\&CC social services and where the children are**

&#x20;   **identified as being at risk of significant harm. Has a verbal and UNOCINI**

&#x20;   **referral been made by the first point of contact (Free text area containing**

&#x20;   **"Yes" and "No" spaces for ticking)**

&#x20;     **- Flags: Identifying "risk of significant harm" requires severe clinical**

&#x20;       **and legal judgment. -> child\_protection\_unocini\_referral\_made\_yes\_no**

&#x20; **- If the children are not considered to be at risk of significant harm does**

&#x20;   **the client wish to avail of a referral to F\&CC social services for family**

&#x20;   **support services (Free text area containing YES/NO text) ->**

&#x20;   **child\_protection\_wish\_family\_support\_referral\_yes\_no**



**Footer Block:**



&#x20; **- Signature \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Date \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ ->**

&#x20;   **child\_protection\_signature, child\_protection\_date**



**Image 16**



**Source Form: Mental Health Assessment (Paper Form)**



**1. Mental Health Occupational Needs**



&#x20; **- Label: Mental Health Occupational Needs (Bold)**

&#x20; **- Type: Free text box (empty in the image).**

&#x20; **- Flags: Bold heading implies mandatory.**

&#x20; **- Proposed JSON Key: mental\_health\_occupational\_needs**



**2. Substance Misuse**



&#x20; **- Label: Substance Misuse (Bold)**

&#x20; **- Type: Free text box divided by three italicized prompts.**

&#x20; **- Prompt 1: Current substance use- (alcohol/drugs-Source) increased in amount,**

&#x20;   **frequency/duration, withdrawal symptoms, and cravings. Polysubstance use,**

&#x20;   **what impact has it had on your life? -> substance\_misuse\_current\_use**

&#x20; **- Prompt 2: Previous substance use- periods of abstinence did you previously**

&#x20;   **engage in? Outcome -> substance\_misuse\_previous\_use**

&#x20; **- Prompt 3: Complex factors- Pregnancy, Injecting or ever injected? (Previous**

&#x20;   **blood test - HIV, HEP B, HEP C (Note: Missing closing parenthesis copied**

&#x20;   **exactly as printed on form) -> substance\_misuse\_complex\_factors**

&#x20; **- Flags: Bold heading implies mandatory. Synthesizing "impact" and identifying**

&#x20;   **"withdrawal symptoms" requires clinical judgment.**



**3. Alcohol Screening Questionnaire (AUDIT-C)**



&#x20; **- Structure: A boxed grid table with 5 rows (including headers/footer) and 7**

&#x20;   **columns.**

&#x20; **- Column Headers:**

&#x20;     **- Questions (Bold)**

&#x20;     **- Scoring system (Bold, spans 5 sub-columns labeled 0, 1, 2, 3, 4)**

&#x20;     **- Score (Bold)**

&#x20; **- Row 1 (Question 1):**

&#x20;     **- How often do you have a drink containing alcohol? (Bold)**

&#x20;     **- Options: Never (0), Monthly or less (1), 2 - 4 times per month (2), 2**

&#x20;       **- 3 times per week (3), 4+ times per week (4) -> audit\_c\_q1**

&#x20; **- Row 2 (Question 2):**

&#x20;     **- How many units of alcohol do you drink on a typical day when you are**

&#x20;       **drinking? (Bold)**

&#x20;     **- Options: 1 -2 (0), 3 - 4 (1), 5 - 6 (2), 7 - 9 (3), 10+ (4) ->**

&#x20;       **audit\_c\_q2**

&#x20; **- Row 3 (Question 3):**

&#x20;     **- How often have you had 6 or more units if female, or 8 or more if male,**

&#x20;       **on a single occasion in the last year? (Bold)**

&#x20;     **- Options: Never (0), Less than monthly (1), Monthly (2), Weekly (3),**

&#x20;       **Daily or almost daily (4) -> audit\_c\_q3**

&#x20; **- Row 4 (Footer / Units Guide):**

&#x20;     **- Cell 1: UNITS GUIDE \\n Max/week Male = 14 \\n Max/week Female = 14**

&#x20;     **- Cell 2: Pint of beer/lager/cider (568ml) 2 UNITS**

&#x20;     **- Cell 3: 1 single measure of spirits (35ml) 1.4 UNITS**

&#x20;     **- Cell 4: Bottle of wine 9 UNITS**

&#x20;     **- Cell 5: 1 medium glass of wine (175ml) 2 UNITS**

&#x20;     **- Cell 6: Bottle of alcopop (275ml) 1.4 UNITS**

&#x20;     **- Cell 7: TOTAL (Blank box for final score) -> audit\_c\_total\_score**

&#x20; **- Flags: Highly structured, boxed, scored questionnaire implies a strict**

&#x20;   **mandatory assessment tool.**



**Please go ahead and upload the next batch of images whenever you are ready!**







**Thank you for the fourth batch. I have carefully extracted all details from**

**these 4 paper forms. Several of these pages feature handwritten clinical notes,**

**which I have transcribed verbatim. Where text has been intentionally obscured**

**with marker, I have noted it.**



**Here is the exhaustive extraction for Batch 5 (Images 17 - 20):**



**Image 17**



**Source Form: Mental State Examination (Continuation, Page 9) Footer text:**

**Unscheduled Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 9**



**Section Heading: Mental State Examination (Bold)**



&#x20; **- Flags: Formatting implies a mandatory, primary clinical document.**



**Domain 1: Appearance and Behaviour**



&#x20; **- Label: Appearance and Behaviour (clothes, self-care, motor activity, rapport**

&#x20;   **and eye-contact) (Bold label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "kempt, Good eye contact" (Note: There is faint**

&#x20;   **text visible beneath this, but it is bleed-through from the reverse side of**

&#x20;   **the page and not written in this box).**

&#x20; **- Flags: Bold heading implies mandatory. Factual observation.**

&#x20; **- Proposed JSON Key: mse\_appearance\_behaviour\_2**



**Domain 2: Speech / Thought Form**



&#x20; **- Label: Speech / Thought Form (spontaneity, coherence, rate, tone, volume,**

&#x20;   **thought disorder) (Bold label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "spontaneous, coherent normal, rate, tone volume**

&#x20;   **nil t.d" (Note: 't.d' is a common clinical abbreviation for thought**

&#x20;   **disorder).**

&#x20; **- Flags: Bold heading implies mandatory. Assessing "thought disorder" requires**

&#x20;   **clinical judgment.**

&#x20; **- Proposed JSON Key: mse\_speech\_thought\_form\_2**



**Domain 3: Mood and Affect**



&#x20; **- Label: Mood and Affect (subjectively + objectively, fluctuation in mood,**

&#x20;   **depressed/not depressed/euthymic/elated, anxiety / panic, reactivity) (Bold**

&#x20;   **label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "I'm happy I have spoken to you. hard to put a**

&#x20;   **number on my mood maybe 5."**

&#x20; **- Flags: Bold heading implies mandatory. Requires clinical judgment to**

&#x20;   **evaluate objective vs. subjective mood.**

&#x20; **- Proposed JSON Key: mse\_mood\_affect\_2**



**Domain 4: Suicidality**



&#x20; **- Label: Suicidality (TLNWL, Suicidal ideation, Suicidal plan or intent, TSH,**

&#x20;   **Self-harm plan or intent) (Bold label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "No TLNWL, No SI"**

&#x20; **- Flags: Bold heading implies mandatory. Extreme clinical judgment required to**

&#x20;   **assess risk and intent.**

&#x20; **- Proposed JSON Key: mse\_suicidality\_2**



**Image 18**



**Source Form: Assessment Proforma Continuation (Page 8) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 8**



**Section 1: Premorbid Personality**



&#x20; **- Label: Premorbid Personality (Bold)**

&#x20; **- Prompt: Attitudes and beliefs, hobbies / interests, religion, friendships,**

&#x20;   **usual coping strategies**

&#x20; **- Type: Free text box.**

&#x20; **- Handwritten Transcription:\[Blank. Visible text is just heavy bleed-through**

&#x20;   **from the other side of the page].**

&#x20; **- Flags: Bold heading implies mandatory. Requires clinical judgment to**

&#x20;   **formulate a baseline personality profile prior to illness.**

&#x20; **- Proposed JSON Key: premorbid\_personality**



**Section 2: Forensic History**



&#x20; **- Label: Forensic History (Bold)**

&#x20; **- Prompt: History of violence and aggression, offences, cautions, custodial**

&#x20;   **sentences, pending charges and court dates**

&#x20; **- Type: Free text box.**

&#x20; **- Handwritten Transcription:\[Blank. Visible text is just heavy bleed-through**

&#x20;   **from the other side of the page].**

&#x20; **- Flags: Bold heading implies mandatory. Mostly factual historical data**

&#x20;   **gathering.**

&#x20; **- Proposed JSON Key: forensic\_history**



**Image 19**



**Source Form: Belfast Trust Emergency Mental Health Assessment Proforma (Front**

**Page) Footer text: Unscheduled Care Team, Fairview 1, 47-51 Crumlin Road,**

**Belfast, BT14 6AB. 1**



**Administrative Top Header Checkboxes:**



&#x20; **- PARIS ASSESSMENT COMPLETED ☐ -> paris\_assessment\_completed**

&#x20; **- ACTIVITY COMPLETED BY PRACTITIONER ☐ -> activity\_completed\_by\_practitioner**



**Section: Patient Details**



&#x20; **- Label: Belfast Trust Emergency Mental Health Assessment Proforma (Bold**

&#x20;   **title)**

&#x20; **- Label: Patient Details (Bold section header)**

&#x20; **- Structure: A multi-cell boxed grid heavily filled with handwriting, some of**

&#x20;   **which is redacted with blue marker.**

&#x20; **- Row 1:**

&#x20;     **- Name (Handwriting: \[Redacted with blue marker]) -> patient\_name\_2**

&#x20;     **- Referral Agent (Handwriting: "GP.") -> referral\_agent\_2**

&#x20; **- Row 2:**

&#x20;     **- Address (Handwriting: \[Redacted with blue marker]) -> patient\_address\_2**

&#x20;     **- Date (Handwriting: "8.1.21") -> assessment\_date\_2 (Flag: Date is bolded,**

&#x20;       **strictly mandatory)**

&#x20; **- Row 3:**

&#x20;     **- DOB (Handwriting: "21.8.60") -> patient\_dob\_2**

&#x20;     **- Age (Handwriting: "60") -> patient\_age\_2**

&#x20;     **- Time Referral Received at USCT (Blank) -> time\_referral\_received\_usct**

&#x20; **- Row 4:**

&#x20;     **- Patient's Contact Number (Handwriting: "90643113") ->**

&#x20;       **patient\_contact\_number\_2**

&#x20;     **- Time Assessment Started: (Handwriting: "1400") ->**

&#x20;       **time\_assessment\_started\_2**

&#x20; **- Row 5:**

&#x20;     **- (Patient's Contact Number spans down)**

&#x20;     **- Time Assessment Finished: (Handwriting: "1500") ->**

&#x20;       **time\_assessment\_finished\_2**

&#x20; **- Row 6:**

&#x20;     **- Next of Kin (Handwriting: \[Redacted with blue marker]) ->**

&#x20;       **next\_of\_kin\_name\_2**

&#x20;     **- Time Follow up completed: (Handwriting: "2000 -") ->**

&#x20;       **time\_follow\_up\_completed**

&#x20; **- Row 7:**

&#x20;     **- Next of Kin Contact Number (Handwriting: \[Redacted with blue marker]) ->**

&#x20;       **next\_of\_kin\_contact\_number\_2**

&#x20;     **- Assessor's Name (Handwriting: \[Redacted with blue marker]) ->**

&#x20;       **assessor\_name\_2**

&#x20; **- Row 8:**

&#x20;     **- Place of Assessment (Handwriting: "own home." followed by \[Redacted**

&#x20;       **text] and the word "carer - " followed by \[Redacted text]) ->**

&#x20;       **place\_of\_assessment\_2**

&#x20;     **- GP's Name (Handwriting: "Dr Gilleland") -> gp\_name\_2**

&#x20; **- Row 9:**

&#x20;     **- (Place of Assessment spans down)**

&#x20;     **- GP's Address (Handwriting: "University Street Surgery") -> gp\_address\_2**

&#x20; **- Row 10:**

&#x20;     **- (Place of Assessment spans down)**

&#x20;     **- GP's Contact Number (Handwriting: "90311118.") -> gp\_contact\_number\_2**



**Section: Other Professionals / Services Involved/Key Worker**



&#x20; **- Label: Other Professionals / Services Involved/Key Worker (Bold)**

&#x20; **- Structure: Grid with 3 columns and 3 rows.**

&#x20; **- Column Headers: Name, Service, Contact Number**

&#x20; **- Handwritten Transcription: Under 'Name' in the first row, "none" is written.**

&#x20;   **The rest is blank.**

&#x20; **- Proposed JSON Keys: other\_prof\_name\_row1, other\_prof\_service\_row1,**

&#x20;   **other\_prof\_contact\_row1 (etc.)**



**Section: Administrative Checkbox \& Referrer Info**



&#x20; **- CONFIDENTIALITY DISCUSSED ☐ (Bold label) -> confidentiality\_discussed\_2**

&#x20;   **(Flag: Bold indicates mandatory administrative step)**

&#x20; **- Clinical information from referrer (Bold label over a large blank box. Heavy**

&#x20;   **bleed-through visible from the reverse page). ->**

&#x20;   **clinical\_information\_referrer\_2**



**Image 20**



**Source Form: Assessment Proforma Continuation (Page 5) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 5**



**Section: Family History**



&#x20; **- Label: Family History (Bold)**

&#x20; **- Prompt: Parents, siblings, atmosphere at home, family history of mental**

&#x20;   **illness / suicide / addiction**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "1 brother, 1 sister father - breakdown -**

&#x20;   **financial concerns 20yrs ago ECT mum - nil, both parents alive - in care**

&#x20;   **homes both have dementia. brother - breakdown 3yrs ago - his son-in-law died**

&#x20;   **in shocking way detained admission sister depression."**

&#x20; **- Flags: Bold heading implies mandatory.**

&#x20; **- Proposed JSON Key: family\_history\_details**



**Section: Personal History**



&#x20; **- Label: Personal History (Bold, serves as a master header for the remaining**

&#x20;   **boxes below).**



&#x20; **- Subsection 1: Childhood**



&#x20;     **- Label: Childhood (Bold)**

&#x20;     **- Prompt: Main caregiver in childhood, childhood trauma (including abuse**

&#x20;       **and witnessing of domestic violence)**

&#x20;     **- Handwritten Transcription: "lovely childhood even during troubles no**

&#x20;       **traumatic events, no sexual abuse, no domestic"**

&#x20;     **- Flags: Assessing trauma requires careful clinical interviewing and**

&#x20;       **judgment. -> personal\_history\_childhood**



&#x20; **- Subsection 2: Education**



&#x20;     **- Label: Education (Bold)**

&#x20;     **- Prompt: Schools, relationship with teachers and peers, behavioural**

&#x20;       **issues, qualifications**

&#x20;     **- Handwritten Transcription: "Got 11+ Went to St. Thomas' Whiterock Rd**

&#x20;       **Older brother looked" (Note: Handwriting trails off/cuts off here, reads**

&#x20;       **"looked")**

&#x20;     **- Flags: Mostly factual history. -> personal\_history\_education**



&#x20; **- Subsection 3: Employment**



&#x20;     **- Label: Employment (Bold)**

&#x20;     **- Handwritten Transcription: "teacher St. Dominics for 25-26yrs head of**

&#x20;       **french enjoyed job"**

&#x20;     **- Flags: Factual history. -> personal\_history\_employment**



&#x20; **- Subsection 4: Relationships**



&#x20;     **- Label: Relationships (Bold)**

&#x20;     **- Prompt: Long-term relationships, psycho-sexual history, children**

&#x20;     **- Handwritten Transcription: "Married since '94"**

&#x20;     **- Flags: Factual history. -> personal\_history\_relationships**



&#x20; **- Subsection 5: Social circumstances**



&#x20;     **- Label: Social circumstances (Bold)**

&#x20;     **- Prompt: Housing, finances, benefits, debt, support systems**

&#x20;     **- Handwritten Transcription: "private nil debt wife nuala my rock"**

&#x20;     **- Flags: Identifying "support systems" requires clinical synthesis. ->**

&#x20;       **personal\_history\_social\_circumstances**



**Please go ahead and upload the next batch of images whenever you are ready!**





**Thank you for the fifth batch. I have carefully extracted all details from**

**these 4 paper forms, transcribing the handwritten clinical notes verbatim and**

**identifying the table structures and scoring guides exactly as requested.**



**Here is the exhaustive extraction for Batch 6 (Images 21 - 24):**



**Image 21**



**Source Form: Assessment Proforma Continuation (Page 6) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 6**



**Section 1: Substance Misuse**



&#x20; **- Label: Substance Misuse (Bold)**

&#x20; **- Prompt: Current use of alcohol and illicit substances, relevant longitudinal**

&#x20;   **history, features of dependency, previous contact with addictions services**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "No drugs No alcohol Never took drugs drank 2-3**

&#x20;   **beers as a student - never smoked."**

&#x20; **- Flags: Bold heading implies a mandatory section. Requires clinical judgment**

&#x20;   **to assess features of dependency.**

&#x20; **- Proposed JSON Key: substance\_misuse\_details\_2**



**Section 2: Alcohol Screening Questionnaire (AUDIT-C variant)**



&#x20; **- Structure: Boxed grid with 5 rows and 7 columns.**

&#x20; **- Column Headers:**

&#x20;     **- Questions (Bold)**

&#x20;     **- Scoring system (Bold, spans 5 sub-columns labeled 0, 1, 2, 3, 4)**

&#x20;     **- Score (Bold)**

&#x20; **- Row 1:**

&#x20;     **- How often do you have a drink containing alcohol? (Bold)**

&#x20;     **- Options: Never (0), Monthly or less (1), 2 - 4 times per month (2), 2**

&#x20;       **- 3 times per week (3), 4+ times per week (4) -> audit\_c\_q1\_2**

&#x20; **- Row 2:**

&#x20;     **- How many units of alcohol do you drink on a typical day when you are**

&#x20;       **drinking? (Bold)**

&#x20;     **- Options: 1 -2 (0), 3 - 4 (1), 5 - 6 (2), 7 - 9 (3), 10+ (4) ->**

&#x20;       **audit\_c\_q2\_2**

&#x20; **- Row 3:**

&#x20;     **- How often have you had 6 or more units if female, or 8 or more if male,**

&#x20;       **on a single occasion in the last year? (Bold)**

&#x20;     **- Options: Never (0), Less than monthly (1), Monthly (2), Weekly (3),**

&#x20;       **Daily or almost daily (4) -> audit\_c\_q3\_2**

&#x20; **- Row 4 (Footer / Units Guide):**

&#x20;     **- Cell 1: UNITS \\n Max/week Male = 21 \\n Max/week Female = 14 (Note: This**

&#x20;       **form uses the older 21-unit male guidance)**

&#x20;     **- Cell 2: Pint of beer/lager/cider (568ml) 2 UNITS**

&#x20;     **- Cell 3: 1 single measure of spirits (35ml) 1.4 UNITS**

&#x20;     **- Cell 4: Bottle of wine 9 UNITS**

&#x20;     **- Cell 5: 1 medium glass of wine (175ml) 2 UNITS**

&#x20;     **- Cell 6: Bottle of alcopop (275ml) 1.4 UNITS**

&#x20; **- Row 5 (Total):**

&#x20;     **- Total score (With a blank box in the 'Score' column) ->**

&#x20;       **audit\_c\_total\_score\_2**

&#x20; **- Scoring Interpretation Guide (Bottom of page):**

&#x20;     **- Score ≥ 4 women – offer brief advice**

&#x20;     **- Score ≥ 5 men – offer brief advice**

&#x20;     **- Score ≥ 8 Refer to alcohol liaison nurs (text cuts off at edge of page)**

&#x20; **- Flags: Highly structured, boxed, scored questionnaire implies a strict**

&#x20;   **mandatory assessment tool.**



**Image 22**



**Source Form: Assessment Proforma Continuation (Page 2) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 2**



**Section: History of Presenting Complaint**



&#x20; **- Label: History of Presenting Complaint (Bold)**

&#x20; **- Prompt: Reason for presentation, precipitating factors, course and duration**

&#x20;   **of problem, features of mental illness, management to date**

&#x20; **- Type: Large free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "I have a long term lung disease - auto immune.**

&#x20;   **shielding since March lost 1 stone since March loss loss of physical tests**

&#x20;   **(Note: first 'loss' is scribbled out) started on Tamsulosin as difficult to**

&#x20;   **pass urine used to love reading used to go to gym, used to walk. working as**

&#x20;   **teacher retired @ 60yrs - Aug '20 hard to eat - no appetite. terrified of**

&#x20;   **covid 19 - used to go for walks but too many people Anxiety creeped up**

&#x20;   **around end of July - started feeling tired - thought my illness came back**

&#x20;   **but it hadn't seen Michelle Henry Rheumatologist, see respiratory team**

&#x20;   **prednisolone helps. loss of appetite, mood is down - cant concentrate, can't**

&#x20;   **read. some days better than others. Never slept well frightened of you**

&#x20;   **coming - needed diazepam doesn't feel loss of role retiring affecting M.H.**

&#x20;   **main reason - fear of covid"**

&#x20; **- Flags: Bold heading indicates a mandatory, core clinical section. High**

&#x20;   **clinical judgment required to synthesize this narrative into a formulation**

&#x20;   **of the patient's presentation.**

&#x20; **- Proposed JSON Key: history\_of\_presenting\_complaint\_2**



**Image 23**



**Source Form: Assessment Proforma Continuation (Page 4) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 4**



**Section 1: Past Psychiatric History**



&#x20; **- Label: Past Psychiatric History (Bold)**

&#x20; **- Prompt: Diagnosis, previous contact with service, details of admissions,**

&#x20;   **treatment to date, use of MHO, details of previous self-harm**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "No formal history of M.I. some contact with**

&#x20;   **Praxis No hospital taking Olanzapine since Nov '20 - no real difference"**

&#x20; **- Flags: Bold heading implies mandatory. Extracting details of "previous**

&#x20;   **self-harm" requires clinical sensitivity and judgment.**

&#x20; **- Proposed JSON Key: past\_psychiatric\_history\_2**



**Section 2: Past Medical History**



&#x20; **- Label: Past Medical History (Bold)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "Fibrosis of lungs - not active @ moment - take**

&#x20;   **prednisolone when active"**

&#x20; **- Flags: Bold heading implies mandatory.**

&#x20; **- Proposed JSON Key: past\_medical\_history\_2**



**Section 3: Medication**



&#x20; **- Label: Medication (Bold)**

&#x20; **- Prompt: Regular medication, dosage, side-effects, allergies, compliance**

&#x20;   **issues**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "See Attached compliant with all meds - no side**

&#x20;   **effects doesn't find Tamsulosin beneficial taking Galfer, folic, nutricia**

&#x20;   **drinks, Vit D. Medicare forrestside."**

&#x20; **- Flags: Bold heading implies mandatory. Evaluating "compliance issues" and**

&#x20;   **"side-effects" requires direct clinical judgment.**

&#x20; **- Proposed JSON Key: medication\_details\_2**



**Image 24**



**Source Form: Assessment Proforma Continuation (Page 10) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 10**



**Domain 1: Thought Content**



&#x20; **- Label: Thought Content (preoccupations, obsessions, delusions, paranoid**

&#x20;   **thoughts, thought interference /- withdrawal/insertion/broadcast/echo,**

&#x20;   **passivity phenomena, and violent thoughts/thoughts to harm others, ideas of**

&#x20;   **reference) (Bold label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "No hallucinations No paranoia No thought**

&#x20;   **interference"**

&#x20; **- Flags: This is a mandatory domain of the MSE. Extremely high clinical**

&#x20;   **judgment is required to probe for and assess complex psychopathology like**

&#x20;   **passivity phenomena and thought insertion.**

&#x20; **- Proposed JSON Key: mse\_thought\_content\_2**



**Domain 2: Perception in all modalities**



&#x20; **- Label: Perception in all modalities (Bold)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "Nil"**

&#x20; **- Flags: Mandatory domain. Clinical judgment needed to distinguish true**

&#x20;   **perceptual disturbances.**

&#x20; **- Proposed JSON Key: mse\_perception\_2**



**Domain 3: Cognition**



&#x20; **- Label: Cognition (orientation, attention, memory– consider MMSE) (Bold**

&#x20;   **label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "feels recall poor mth ✔ yr ✔ 7th. no difference**

&#x20;   **in co-ordination." (Note: The ticks '✔' beside 'mth' \[month] and 'yr' \[year]**

&#x20;   **indicate correct orientation. '7th' likely refers to the Serial 7s**

&#x20;   **subtraction test).**

&#x20; **- Flags: Mandatory domain. Clinical judgment used to administer and interpret**

&#x20;   **cognitive testing on the fly.**

&#x20; **- Proposed JSON Key: mse\_cognition\_2**



**Domain 4: Insight**



&#x20; **- Label: Insight (awareness of illness and need for help, willingness to**

&#x20;   **engage) (Bold label, standard text prompt)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "know something isnt right, would do anything to**

&#x20;   **get better - keen to know if he can be helped."**

&#x20; **- Flags: Mandatory domain. Pure clinical judgment metric assessing the**

&#x20;   **patient's internal understanding of their illness.**

&#x20; **- Proposed JSON Key: mse\_insight\_2**



**Please go ahead and upload the next batch of images whenever you are ready!**





**I am combining the 3 images you uploaded in your previous message with the 1**

**image you just uploaded to form Batch 7 (Images 25 - 28).**



**I have carefully extracted all details from these 4 pages, transcribing**

**handwriting verbatim, capturing table structures, and flagging**

**mandatory/judgment fields as requested.**



**Here is the exhaustive extraction for Batch 7:**



**Image 25**



**Source Form: Assessment Proforma Continuation (Page 3) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 3**



**Section 1: History of Presenting Continued**



&#x20; **- Label: History of Presenting Continued (Bold)**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "Great relationship with wife - 'my rock' walks**

&#x20;   **from home - shes super, looks after me, my meds + deals with GP I have a**

&#x20;   **strong urge to pass urine but can't, it will feel like a long day ahead,**

&#x20;   **Nuala counts \& checks my medication breakfast together was teaching myself**

&#x20;   **to learn Italian read before bed bed @ 10pm sleep 1030 - lost my confidence,**

&#x20;   **I feel desperation as I'm a burden, will do anything to get better - go to**

&#x20;   **hospital"**

&#x20; **- Margin Note (written outside/below the box): "uncle SI hanging."**

&#x20;   **(Abbreviation 'SI' = Suicidal Ideation / Suicide Incident)**

&#x20; **- Flags: Bold heading implies mandatory continuation. Requires clinical**

&#x20;   **judgment to synthesize presentation.**

&#x20; **- Proposed JSON Key: history\_of\_presenting\_continued\_3**



**Section 2: Collateral History**



&#x20; **- Label: Collateral History (if not completed, please indicate reason) (Bold)**

&#x20; **- Prompt 1: Does the patient consent to a collateral history being obtained?**

&#x20;   **Yes / No (Bold, 'Yes' is circled) -> collateral\_consent\_obtained\_yes\_no**

&#x20; **- Prompt 2: Issues of concern, support systems, limitations of carer's**

&#x20; **- Type: Free text box containing handwritten notes.**

&#x20; **- Handwritten Transcription: "huge difference since July, was outgoing, funny**

&#x20;   **- interested in Art/literature, lost 1stone - lots of tests around lung**

&#x20;   **disease - pain in side - difficult to urinate ? cancer - NAD ended up**

&#x20;   **related to anxiety - mild depression started Mirtazapine 2mg diazepam.**

&#x20;   **Oct 7th Anxiety ↑↑ went to RVH thought something wrong - admitted**

&#x20;   **hyponatraemia Nil medically - wife wanted psych assessment Consultant - they**

&#x20;   **only see ppl actively suicidal. Woodstock input Relationship of informant to**

&#x20;   **patient ↑ mirtazapine ↑ diazepam by GP (Note: "Relationship of informant to**

&#x20;   **patient" is pre-printed text, the handwriting flows over and around it)**

&#x20;   **woodstock - 6 sessions CBT - Roger pacing, rocking on floor crying Rory GP**

&#x20;   **suggested ↑ diazepam. went to private c kelly - assessment ↑ Mirtazapine ↑**

&#x20;   **to 45mg. 3x 2.5 olanzapine 2-3x 5mg diazepam last seen Chris kelly, Wife**

&#x20;   **says Roger would rather be dead."**

&#x20; **- Flags: Bold heading implies a mandatory section. Extreme clinical judgment**

&#x20;   **required to interpret collateral risk factors (e.g., "would rather be dead",**

&#x20;   **pacing, medication changes).**

&#x20; **- Proposed JSON Key: collateral\_history\_details**



**Image 26**



**Source Form: Assessment Proforma Continuation (Page 7) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 7**



**Section Heading: Leeds Dependence Questionnaire (Bold)**



**1. Scoring Criteria Table (Top)**



&#x20; **- Structure: 3-column grid containing text criteria.**

&#x20; **- Cell 1: LDQ score 10-22 and drinking over 15 units a day (moderate**

&#x20;   **dependence) Plus complex issues**

&#x20; **- Cell 2: AUDIT 20 or more, and LDQ score over 22, and drinking over 30 units**

&#x20;   **a day**

&#x20; **- Cell 3: AUDIT 20 or more, and LDQ score 10-22 and drinking over 15 units a**

&#x20;   **day (moderate dependence), Plus complex issues**



**2. Complex Issues Checklist Box**



&#x20; **- Label: Complex issues:**

&#x20; **- Bullet Points:**

&#x20;     **- Patient pregnant**

&#x20;     **- Clear history of dependence with clear physical/psychological/social**

&#x20;       **consequences**

&#x20;     **- Clear need for medical intervention**

&#x20;     **- IV drug use**

&#x20;     **- Failed treatment in the past with voluntary sector**

&#x20;     **- Multiple substances dependence**



**3. Questionnaire Body**



&#x20; **- Instructions: Think about the main substance you have been using over the**

&#x20;   **last 4 weeks (Bold)**

&#x20; **- Scoring Key: Never -0 Sometimes -1 Often -2 Nearly Always -3 (Bold)**

&#x20; **- Questions (Each requires a checkbox selection):**

&#x20;   **1.  Do you think about when you will next be able to have another drink or**

&#x20;       **take more drugs?**

&#x20;   **2.  Is drinking or taking drugs more important than anything else you might**

&#x20;       **do during the day?**

&#x20;   **3.  Do you feel that your need for drink or drugs is too strong to control**

&#x20;   **4.  Do you plan your days around getting and taking drink or drugs?**

&#x20;   **5.  Do you drink or take drugs in a particular way in order to increase the**

&#x20;       **effect it gives you?**

&#x20;   **6.  Do you drink or take drugs morning, afternoon and evening?**

&#x20;   **7.  Do you feel you have to carry on drinking or taking drugs once you have**

&#x20;       **started?**

&#x20;   **8.  Is getting an effect more important than the particular drink or drug**

&#x20;       **you use?**

&#x20;   **9.  Do you want to take more drink or drugs when the effects start to wear**

&#x20;       **off?**

&#x20;   **10. Do you find it difficult to cope with life without drink or drugs?**

&#x20; **- Total Box: Score \[Blank Checkbox] -> ldq\_total\_score\_page7**



**4. Clinical Guidance Footer**



&#x20; **- Text 1: Considering the score and if the patient has any complex issues, if**

&#x20;   **patient has a score of 20 or more consider onward referral to CAT.**

&#x20; **- Text 2: If below this score consider referral to voluntary services, for**

&#x20;   **example, Addictions NI, Ascert and Dunlewy. Or Daisy if under the age of 25.**

&#x20; **- Flags: Structured, scored questionnaire implying strict mandatory**

&#x20;   **completion. Clinical judgment is explicitly required by the form to**

&#x20;   **integrate the score with "complex issues" for onward referral.**



**Image 27**



**Source Form: Assessment Proforma Continuation (Page 11) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 11 (Note: This is**

**a blank copy of the Child Protection form seen previously, but is explicitly**

**included here as page 11 of this patient's specific assessment).**



**Section Heading: Child Protection Details (Bold)**



&#x20; **- Flags: Heavily boxed and structured. Mandatory legal/safeguarding document.**



**Demographics Block:**



&#x20; **- Patient's Name (Free text) -> child\_protection\_patient\_name\_p11**

&#x20; **- Marital Status: Married / Separated / Divorced / Single / Widowed (delete as**

&#x20;   **appropriate) -> child\_protection\_marital\_status\_p11**

&#x20; **- Partner's name (Free text) -> child\_protection\_partner\_name\_p11**

&#x20; **- Partner's Address (Free text) -> child\_protection\_partner\_address\_p11**



**Children Contact Table:**



&#x20; **- Structure: Grid with 7 columns and 5 blank rows.**

&#x20; **- Column Headers (All Bold): Names of children with whom patient has regular**

&#x20;   **contact | Sex | D.O.B | Relationship to patient | Relationship to partner |**

&#x20;   **Name of mother/father if they are not living with child | Do they have**

&#x20;   **contact with the child?**



**Safeguarding Action Blocks:**



&#x20; **- Block 1: Are Family \& Child Care (F\&CC) Social Services involved with the**

&#x20;   **family? (Yes / No fields) -> child\_protection\_fcc\_involved\_yes\_no\_p11**

&#x20; **- Block 1a: If yes, name of staff involved \& contact details (when last**

&#x20;   **involved – reasons for involvement / reasons for discharge) (Free text)**

&#x20; **- Block 2: For families not known to F\&CC social services and where the**

&#x20;   **children are identified as being at risk of significant harm. Has a verbal**

&#x20;   **and UNOCINI referral been made by the first point of contact (Yes / No**

&#x20;   **fields)**

&#x20;     **- (Flag: The UNOCINI referral question is printed in red ink, indicating a**

&#x20;       **critical, legally mandatory step if risk is identified).**

&#x20; **- Block 2a: If the children are not considered to be at risk of significant**

&#x20;   **harm does the client wish to avail of a referral to F\&CC social services for**

&#x20;   **family support services (YES/NO field)**

&#x20; **- Footer: Signature \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Date \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**



**Image 28**



**Source Form: Assessment Proforma Continuation (Page 14) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 14**



**Structure: A structured grid for categorical risk screening and narrative**

**summaries.**



**Section 1: VIOLENCE \& AGGRESSION**



&#x20; **- Header: VIOLENCE \& AGGRESSION (Bold) | Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page beneath the header.**

&#x20; **- Proposed JSON Keys: risk\_violence\_aggression\_category,**

&#x20;   **risk\_violence\_aggression\_details**



**Section 2: POTENTIAL DISENGAGEMENT/LOSS OF CONTACT/NON-COMPLIANCE/ABSCONDING**



&#x20; **- Header: POTENTIAL DISENGAGEMENT/LOSS OF CONTACT/NON-COMPLIANCE/ABSCONDING**

&#x20;   **(Bold) | Yes | No | Unknown**

&#x20; **- Type: Blank row.**

&#x20; **- Proposed JSON Keys: risk\_disengagement\_category, risk\_disengagement\_details**



**Section 3: AREAS IDENTIFIED FROM MENTALSTATE ASSESSMENT**



&#x20; **- Header: AREAS IDENTIFIED FROM MENTALSTATE ASSESSMENT (Bold) | Yes | No |**

&#x20;   **Unknown (Note: MENTALSTATE missing space on form)**

&#x20; **- Type: Blank row.**

&#x20; **- Proposed JSON Keys: risk\_mse\_areas\_category, risk\_mse\_areas\_details**



**Section 4: OTHER INDICATORS OF RISK**



&#x20; **- Header: OTHER INDICATORS OF RISK (Bold) | Yes | No | Unknown**

&#x20; **- Type: Blank row.**

&#x20; **- Proposed JSON Keys: risk\_other\_indicators\_category,**

&#x20;   **risk\_other\_indicators\_details**



**Section 5: COLLATERAL HISTORY / RELATIONSHIP TO SERVICE USER**



&#x20; **- Header: COLLATERAL HISTORY / RELATIONSHIP TO SERVICE USER (Bold)**

&#x20; **- Type: Large blank box.**

&#x20; **- Proposed JSON Key: risk\_collateral\_history\_summary**



**Section 6: SUMMARY OF ACTIVE RISK**



&#x20; **- Header: SUMMARY OF ACTIVE RISK (Bold)**

&#x20; **- Type: Large blank box.**

&#x20; **- Flags: This is a paramount clinical judgment field. It requires formulating**

&#x20;   **all historical and current data into a cohesive risk summary.**

&#x20; **- Proposed JSON Key: summary\_of\_active\_risk**



**Section 7: SUMMARY OF PROTECTIVE FACTORS**



&#x20; **- Header: SUMMARY OF PROTECTIVE FACTORS (Bold)**

&#x20; **- Type: Large blank box.**

&#x20; **- Flags: Requires clinical judgment to evaluate resilience and external**

&#x20;   **supports.**

&#x20; **- Proposed JSON Key: summary\_of\_protective\_factors**



**(Note: There is significant text bleeding through from the reverse side of this**

**page showing signatures and specialized risk assessments, but I am extracting**

**only what is printed on the face of page 14).**



**Please go ahead and upload the next batch of images whenever you are ready!**







**Thank you for the next batch of images. It looks like the text in your prompt**

**was a copy of my previous response, but I have received the 4 new images you**

**attached. I will treat these as Batch 8 (Images 29 - 32).**



**Here is the exhaustive extraction for these 4 pages, applying your strict rules**

**for formatting, clinical judgment flags, and zero omissions.**



**(Note: These forms contain several typographical errors in the printed text,**

**such as "OFENDING" and "Were signature refused". I have transcribed them exactly**

**as they appear on the page, per your instructions).**



**Image 29**



**Source Form: Assessment Proforma Continuation (Page 13) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 13**



**Top Instruction:**



&#x20; **- PLEASE PROVIDE DETAILS UNDER EACH HEADING (HISTORICAL AND CURRENT) (All**

&#x20;   **caps)**



**Section 1: SELF HARM / SUICIDAL BEHAVIOUR**



&#x20; **- Header: SELF HARM / SUICIDAL BEHAVIOUR (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown (with blank spaces for ticking**

&#x20;   **beneath each)**

&#x20; **- Type: Blank row spanning the width of the page beneath the header.**

&#x20; **- Flags: Boxed formatting implies a mandatory risk section. Assessing**

&#x20;   **historical vs. current intent requires high clinical judgment.**

&#x20; **- Proposed JSON Keys: risk\_self\_harm\_category\_yes\_no\_unknown,**

&#x20;   **risk\_self\_harm\_details**



**Section 2: ALCOHOL/SUBSTANCE MISUSE**



&#x20; **- Header: ALCOHOL/SUBSTANCE MISUSE (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown**

&#x20; **- Sub-question: If there is history of drug use, ever injected not under**

&#x20;   **instruction of doctor (Bold) | Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page.**

&#x20; **- Flags: Boxed/Mandatory. Factual history gathering.**

&#x20; **- Proposed JSON Keys: risk\_substance\_misuse\_category,**

&#x20;   **risk\_substance\_injected\_category, risk\_substance\_misuse\_details**



**Section 3: NEGLECT AND VULNERABILITY**



&#x20; **- Header: NEGLECT AND VULNERABILITY (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page.**

&#x20; **- Flags: Boxed/Mandatory. Requires clinical and safeguarding judgment to**

&#x20;   **identify neglect.**

&#x20; **- Proposed JSON Keys: risk\_neglect\_vulnerability\_category,**

&#x20;   **risk\_neglect\_vulnerability\_details**



**Section 4: CHILDS CARE AND VULNERABLE ADULT ISSUES**



&#x20; **- Header: CHILDS CARE AND VULNERABLE ADULT ISSUES (Specify arrangements for**

&#x20;   **Children) (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page.**

&#x20; **- Flags: Boxed/Mandatory. Strict legal/safeguarding requirement.**

&#x20; **- Proposed JSON Keys: risk\_child\_vulnerable\_adult\_category,**

&#x20;   **risk\_child\_vulnerable\_adult\_details**



**Section 5: PHYSICAL IMPAIRMENT**



&#x20; **- Header: PHYSICAL IMPAIRMENT (e.g. medical/ sensory) (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page.**

&#x20; **- Flags: Boxed/Mandatory. Factual clinical observation.**

&#x20; **- Proposed JSON Keys: risk\_physical\_impairment\_category,**

&#x20;   **risk\_physical\_impairment\_details**



**Section 6: DISSOCIAL OFENDING BEHAVIOUR (Copied exactly as printed with typo)**



&#x20; **- Header: DISSOCIAL OFENDING BEHAVIOUR (Bold)**

&#x20; **- Checkboxes/Options: Yes | No | Unknown**

&#x20; **- Type: Blank row spanning the width of the page.**

&#x20; **- Flags: Boxed/Mandatory. Requires clinical judgment to classify behavior as**

&#x20;   **dissocial.**

&#x20; **- Proposed JSON Keys: risk\_dissocial\_offending\_category,**

&#x20;   **risk\_dissocial\_offending\_details**



**Image 30**



**Source Form: RISK SCREENING TOOL (Page 12) Footer text: Unscheduled Care Team,**

**Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 12**



**Section Heading: RISK SCREENING TOOL (Bold)**



**Demographics Block (Boxed Grid):**



&#x20; **- Row 1:**

&#x20;     **- NAME (Bold) -> risk\_screen\_patient\_name**

&#x20;     **- DOB (Bold) -> risk\_screen\_patient\_dob**

&#x20;     **- DATE (Bold) -> risk\_screen\_date**

&#x20;     **- TIME (Bold) -> risk\_screen\_time**

&#x20; **- Row 2:**

&#x20;     **- Outpatient/ community (Bold) -> risk\_screen\_setting\_outpatient**

&#x20;     **- Inpatient (Insert Hosp No.) (Bold) -> risk\_screen\_setting\_inpatient\_no**

&#x20;     **- Voluntary (Bold) -> risk\_screen\_status\_voluntary**

&#x20;     **- Detained (Bold) -> risk\_screen\_status\_detained**



**Information Sources Grid:**



&#x20; **- Header: INFORMATION SOURCES AVAILABLE / ACCESSED ON COMPLETING RISK HISTORY**

&#x20;   **(All Caps, Bold)**

&#x20; **- Structure: Grid with 8 rows and 3 columns. Column 1 is the source, Column 2**

&#x20;   **has the prompt "Specify:", and Column 3 is a blank box for a tick or further**

&#x20;   **text.**

&#x20; **- Rows:**

&#x20;   **1.  Key Worker / Team Leader | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_key\_worker\_specify, info\_source\_key\_worker\_check**

&#x20;   **2.  Service user | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_service\_user\_specify, info\_source\_service\_user\_check**

&#x20;   **3.  Clinical notes | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_clinical\_notes\_specify, info\_source\_clinical\_notes\_check**

&#x20;   **4.  General Practitioner (GP) via referral | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_gp\_referral\_specify, info\_source\_gp\_referral\_check**

&#x20;   **5.  General Practitioner (GP) direct/ by telephone | Specify: | \[Blank Box]**

&#x20;       **-> info\_source\_gp\_direct\_specify, info\_source\_gp\_direct\_check**

&#x20;   **6.  Carer / relative | Specify: |\[Blank Box] ->**

&#x20;       **info\_source\_carer\_relative\_specify, info\_source\_carer\_relative\_check**

&#x20;   **7.  Police / probation services | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_police\_probation\_specify, info\_source\_police\_probation\_check**

&#x20;   **8.  Other (Please Specify) | Specify: | \[Blank Box] ->**

&#x20;       **info\_source\_other\_specify, info\_source\_other\_check**

&#x20; **- Flags: Heavily structured grid implies a mandatory checklist to prove due**

&#x20;   **diligence in risk assessment gathering.**



**Image 31**



**Source Form: Assessment Proforma Continuation (Page 15) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 15**



**Section 1: IMMEDIATE MANAGEMENT PLAN OF IDENTIFIED RISK**



&#x20; **- Header Table:**

&#x20;     **- IMMEDIATE MANAGEMENT PLAN OF IDENTIFIED RISK / ACTION (Bold/Underlined**

&#x20;       **and Bold)**

&#x20;     **- Name of Person(s) Responsible (Bold)**

&#x20;     **- Signed (Bold)**

&#x20; **- Structure: Grid with 5 empty rows beneath the headers.**

&#x20; **- Flags: Mandatory clinical planning document. High clinical judgment required**

&#x20;   **to formulate actionable, safe plans.**

&#x20; **- Proposed JSON Key: immediate\_risk\_management\_plan (Array of objects)**



**Section 2: CONTINGENCY ARRANGEMENTS**



&#x20; **- Label: CONTINGENCY ARRANGEMENTS (Bold)**

&#x20; **- Type: Large blank boxed area.**

&#x20; **- Flags: Mandatory clinical judgment formulation.**

&#x20; **- Proposed JSON Key: contingency\_arrangements**



**Section 3: FURTHER ACTION NECESSARY**



&#x20; **- Label: FURTHER ACTION NECESSARY (Bold)**

&#x20; **- Checkboxes (all formatted with bold text and a square box):**

&#x20;     **- Discuss with Multidisciplinary Team ☐ -> action\_discuss\_mdt**

&#x20;     **- Specialised Risk Assessment ☐ -> action\_specialised\_risk\_assessment**

&#x20;     **- Keep under review ☐ -> action\_keep\_under\_review**

&#x20;     **- Comprehensive Risk Assessment ☐ -> action\_comprehensive\_risk\_assessment**

&#x20;     **- No further action required ☐ -> action\_no\_further\_action\_required**



**Section 4: DISTRIBUTION**



&#x20; **- Label: DISTRIBUTION (Bold)**

&#x20; **- Checkboxes:**

&#x20;     **- Service user ☐ -> distribution\_service\_user**

&#x20;     **- Key Worker ☐ -> distribution\_key\_worker**

&#x20;     **- Other ☐ (specify) \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ -> distribution\_other,**

&#x20;       **distribution\_other\_specify**



**Section 5: Signatures and Administrative Close**



&#x20; **- Service User's signature: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Date: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Refused to sign ☐**

&#x20; **- Were signature refused, indicate reason:**

&#x20;   **\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ (Note: Typo "Were" instead of**

&#x20;   **"Where" copied exactly)**

&#x20; **- Signature: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Date: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Designation \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Contact Tel No:**

&#x20;   **\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Signature: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Date: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Designation \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_ Contact Tel. No.**

&#x20;   **\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_**

&#x20; **- Footer Instruction: On inpatient admission - to be completed jointly by the**

&#x20;   **admitting Doctor and nurse in consultation with the Family/Carers and others**

&#x20;   **(if in attendance at time of admission). (Bold)**

&#x20; **- Flags: Strict mandatory medico-legal sign-off section.**



**Image 32**



**Source Form: RISK SCREENING TOOL – RECORD OF REVIEWS (Page 16) Footer text:**

**Unscheduled Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 16**



**Section Heading: RISK SCREENING TOOL – RECORD OF REVIEWS (Bold)**



**Demographics Block:**



&#x20; **- NAME (Bold) | \[Blank Box] -> risk\_review\_patient\_name**

&#x20; **- DOB (Bold) | \[Blank Box] -> risk\_review\_patient\_dob**



**Record of Reviews Table:**



&#x20; **- Structure: Grid with 5 columns and 5 blank rows. (Note: there is heavy**

&#x20;   **bleed-through text from the reverse page showing through the empty boxes,**

&#x20;   **but the boxes themselves are blank for user input).**

&#x20; **- Column Headers (All Bold):**

&#x20;   **1.  DATE/ TIME -> risk\_review\_date\_time**

&#x20;   **2.  UPDATE/ CHANGE IN RISK -> risk\_review\_update\_change**

&#x20;   **3.  ALTERATION TO RISK MANAGEMENT PLAN -> risk\_review\_alteration\_to\_plan**

&#x20;   **4.  LEAD RESPONSIBILITY -> risk\_review\_lead\_responsibility**

&#x20;   **5.  Signed: -> risk\_review\_signed**

&#x20; **- Flags: Mandatory tracking grid. Updating risk and altering management plans**

&#x20;   **requires significant, ongoing clinical judgment.**

&#x20; **- Proposed JSON Key: risk\_record\_of\_reviews (Array of objects)**



**This completes the exhaustive extraction of these 32 images.**



**You mentioned previously there might be 35 images in total. If you have the**

**remaining 3 images, please upload them now.**



**If we are officially finished with all images, just let me know, and I will**

**generate the Final Master JSON Schema grouped into your requested categories!**









**Thank you for uploading the final batch. I will extract these last 3 images and**

**then compile the Final Master JSON Schema based on all 35 images, exactly as you**

**requested.**



**Here is the exhaustive extraction for the final batch (Images 33 - 35):**



**Image 33**



**Source Form: Assessment Proforma Continuation (Page 18) Footer text: Unscheduled**

**Care Team, Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 18**



**Section: FOLLOW UP TELEPHONE CALLS**



&#x20; **- Label: FOLLOW UP TELEPHONE CALLS (Bold and underlined)**

&#x20; **- Structure: Grid with 2 columns and 22 blank rows.**

&#x20; **- Column Headers:**

&#x20;     **- DATE (Bold)**

&#x20;     **- CONTACTS (Bold)**

&#x20; **- Handwritten Transcription:**

&#x20;     **- Row 1: \[Date column blank] | "T/c tomorrow - 11am ?" (Note: T/c =**

&#x20;       **Telephone call)**

&#x20;     **- Row 2:\[Date column blank] | "V/c Sunday - 12" (Note: V/c = Video call or**

&#x20;       **Voice call)**

&#x20; **- Flags: Factual administrative tracking.**

&#x20; **- Proposed JSON Key: follow\_up\_telephone\_calls\_log (Array of objects)**



**Image 34**



**Source Form: Belfast Trust Emergency Mental Health Assessment Summary (Page 20)**

**Footer text: Unscheduled Care Team, Fairview 1, 47-51 Crumlin Road, Belfast,**

**BT14 6AB. 20**



**Section Heading: Belfast Trust Emergency Mental Health Assessment Summary (Bold)**



**Section 1: Patient's details**



&#x20; **- Label: Patient's details (Bold)**

&#x20; **- Fields:**

&#x20;     **- Name (Free text) -> summary\_patient\_name**

&#x20;     **- Address (Large free text box) -> summary\_patient\_address**

&#x20;     **- DOB (Free text) -> summary\_patient\_dob**

&#x20;     **- GP Details (Large free text box) -> summary\_gp\_details**



**Section 2: Summary Narrative Blocks**



&#x20; **- Summary of History (Large blank box) -> summary\_of\_history (Flag: Requires**

&#x20;   **clinical judgment to summarize)**

&#x20; **- Salient Features of MSE (Large blank box) -> summary\_salient\_features\_mse**

&#x20;   **(Flag: Requires clinical judgment to summarize)**

&#x20; **- Pertinent Risk Issues (Large blank box) -> summary\_pertinent\_risk\_issues**

&#x20;   **(Flag: Requires high clinical judgment to summarize)**



**Section 3: Actions \& Outcomes**



&#x20; **- Child Protection Issues Identified Yes ☐ No ☐ (Bold) ->**

&#x20;   **summary\_child\_protection\_issues\_identified (Flag: Strict mandatory legal**

&#x20;   **assessment)**

&#x20; **- ASP1 Form Yes ☐ No ☐ (Bold) -> summary\_asp1\_form\_completed**

&#x20; **- Management Plan (Large blank box) -> summary\_management\_plan (Flag: Requires**

&#x20;   **clinical formulation)**

&#x20; **- Safety leaflet given Yes ☐ No ☐ (Located inside the Management Plan box) ->**

&#x20;   **summary\_safety\_leaflet\_given**

&#x20; **- Outcome of MDT Discussion (Large blank box) ->**

&#x20;   **summary\_outcome\_mdt\_discussion**



**Section 4: Diagnosis \& Signatures**



&#x20; **- ICD-10 Diagnosis: 1. F\[Box] 2. F\[Box] 3. F\[Box] ->**

&#x20;   **summary\_icd10\_diagnosis\_1, \_2, \_3 (Flag: Pure clinical judgment)**

&#x20; **- Assessor's signature -> summary\_assessor\_signature**

&#x20; **- Time assessment completed -> summary\_time\_assessment\_completed**

&#x20; **- Assessor's name (Print) -> summary\_assessor\_name\_printed**

&#x20; **- Date assessment completed -> summary\_date\_assessment\_completed**

&#x20; **- Designation -> summary\_assessor\_designation**

&#x20; **- Contact number 95 044870 (Pre-printed number)**



**Image 35**



**Source Form: AIDE MEMOIRE (Page 17) Footer text: Unscheduled Care Team,**

**Fairview 1, 47-51 Crumlin Road, Belfast, BT14 6AB. 17**



**Note: This page is an "AIDE MEMOIRE" (a reference guide). It does not contain**

**blank spaces for data entry, but lists risk factors that clinicians must**

**consider when filling out the risk assessments. Per your instructions for zero**

**omissions, here is the exact text of every bullet point on this reference page.**



**Section Heading: AIDE MEMOIRE**



**Left Column Boxes:**



&#x20; **- SELF HARM / SUICIDAL BEHAVIOUR: Current suicidal thoughts, plans; Previous**

&#x20;   **history of suicide attempts / self harm; Suicidal ideation / preoccupation;**

&#x20;   **Family history of suicide / or recent loss; Access to means**

&#x20; **- NEGLECT \& VULNERABILITY: Previous history of self neglect, inadequate**

&#x20;   **housing, poor nutrition, poor hygiene; Current risk of self neglect; Risk of**

&#x20;   **being exploited by others / history of exploitation; At risk of accidental**

&#x20;   **wandering / falls / harm inside or outside the home**

&#x20; **- PHYSICAL IMPAIRMENT: Medical; Sensory**

&#x20; **- VIOLENCE AND AGGRESSION: Previous violence, aggression or assault towards**

&#x20;   **others including - other patients / staff / family / carers / general**

&#x20;   **public; Talking of or planning to harm others; Display high anger,**

&#x20;   **hostility, threatening behaviour; Threats against a particular individual;**

&#x20;   **History of owning, carrying, using weapons; History of property damage;**

&#x20;   **Arson (deliberate fire setting); Sexual assault (includes touching /**

&#x20;   **exposure)**

&#x20; **- MENTAL STATE: Appearance and behaviour; Speech; Mood; Perception, command**

&#x20;   **hallucinations; Cognition; Mini Mental State; Insight; Previous history of**

&#x20;   **serious mental illness; Thought content (over-valued ideas / delusions);**

&#x20;   **Relapse signatures**

&#x20; **- OTHER INDICATORS OF RISK: Recent severe stress; Concern expressed by others;**

&#x20;   **Recurrence of circumstances associated with risk; Impending stressors e.g.**

&#x20;   **court appearance; Abuse / victimisation by others; Social isolation; Lack of**

&#x20;   **social or carer support system; High levels of stress of carer / high carer**

&#x20;   **burden; Volatile personal relationships; Nomadic lifestyle; Housing**

&#x20;   **problems; Severe financial difficulties; Chronic medical illness; Terminal,**

&#x20;   **painful or debilitating illness; Driving**



**Right Column Boxes:**



&#x20; **- ALCOHOL / SUBSTANCE MISUSE: Known history of alcohol / substance abuse;**

&#x20;   **Currently misusing alcohol / substances; Known history of abusing**

&#x20;   **stimulants; Previous non accidental overdose?; Consumption of alcohol,**

&#x20;   **non-prescribed drugs, misuse of prescribed drugs / non concordance;**

&#x20;   **Injecting drug use - see addictions addendum re hepatitis/HIV risk**

&#x20; **- CHILD CARE AND VULNERABLE ADULT ISSUES: How many children? Ages? Carer?**

&#x20;   **Custody arrangements; Vulnerable adult in household; Children currently on**

&#x20;   **child protection register; Involvement of other services, e.g., family and**

&#x20;   **child care team, CAMHS, health visiting; UNOCINI done or needed; Threats**

&#x20;   **violence to any child / children; Emotional abuse or neglect of any child /**

&#x20;   **children; History of domestic violence**

&#x20; **- DISSOCIAL \& OFFENDING BEHAVIOUR: Criminal history, including exclusion**

&#x20;   **orders, bail; Conviction for violent offences; Conviction for sexual**

&#x20;   **offences; Previously been a diagnosis made of psychopathy / antisocial**

&#x20;   **personality disorder; History of containment - Special hospital, Medium**

&#x20;   **Secure Unit, Locked Intensive Care Unit; Dissocial behaviours**

&#x20; **- POTENTIAL DISENGAGEMENT: Previous history of poor concordance with treatment**

&#x20;   **/ medication; Does the person understand his/her illness?; Does the person**

&#x20;   **actively attempt to mislead others with respect to concordance with**

&#x20;   **treatment?; Severe side-effects of medication; Unplanned disengagement from**

&#x20;   **services; History of compulsory admission**

&#x20; **- RELATIONSHIP WITH RELATIVE / CARER: Known history of threat / violence**

&#x20;   **towards the relative / carer; Current risk of threat / violence towards the**

&#x20;   **relative / carer; Known history of abuse towards the client**

&#x20; **- PROTECTIVE FACTORS: Willingness to engage with mental health services;**

&#x20;   **Compliance with medication; Abstinence from alcohol/ drugs; Family/ social**

&#x20;   **support networks; Faith/ religion; Financial security; Support from**

&#x20;   **employer; Weapons removed; Fear of physical injury/ disability after failed**

&#x20;   **attempt**

&#x20; **- IMMEDIATE MANAGEMENT PLAN: Action to be taken; Who is responsible for**

&#x20;   **action; Date responsibility acknowledged; Need for some action to be**

&#x20;   **recorded, even if discharge to GP. If so, record date GP informed.**

&#x20; **- Flags: This entire page dictates the clinical judgment boundaries required**

&#x20;   **to fill out the rest of the paperwork.**



**FINAL MASTER JSON SCHEMA**



**Here is the synthesized master list of every unique data point extracted across**

**all 35 images, converted into clean snake\_case keys and grouped exactly into**

**your requested categories. This is formatted to be directly usable as the schema**

**for your AI system.**



**1. Patient Demographics**



**(Source: Assessment Proforma front page, Child Protection Details, Assessment**

**Summary, Risk Screening Tool)**



&#x20; **- patient\_name**

&#x20; **- health\_and\_care\_number**

&#x20; **- patient\_address**

&#x20; **- patient\_dob**

&#x20; **- patient\_age**

&#x20; **- patient\_contact\_number**

&#x20; **- next\_of\_kin\_name**

&#x20; **- next\_of\_kin\_contact\_number**

&#x20; **- gp\_name**

&#x20; **- gp\_address**

&#x20; **- gp\_contact\_number**



**2. Risk Assessment**



**(Source: PISANI, Risk Screening Tool, Assessment Summary, Epic EHR)**



&#x20; **- presenting\_suicide\_events\_48\_hrs**

&#x20; **- pisani\_strengths\_protective\_factors**

&#x20; **- pisani\_long\_term\_risk\_factors**

&#x20; **- pisani\_impulsivity\_self\_control**

&#x20; **- pisani\_past\_suicidal\_behaviours**

&#x20; **- pisani\_recent\_present\_suicidal\_behaviours**

&#x20; **- pisani\_stressors\_precipitants**

&#x20; **- pisani\_symptoms\_suffering\_recent\_changes**

&#x20; **- pisani\_engagement\_and\_reliability**

&#x20; **- risk\_violence\_aggression\_category\_yes\_no\_unknown**

&#x20; **- risk\_violence\_aggression\_details**

&#x20; **- risk\_disengagement\_category\_yes\_no\_unknown**

&#x20; **- risk\_disengagement\_details**

&#x20; **- risk\_mse\_areas\_category\_yes\_no\_unknown**

&#x20; **- risk\_mse\_areas\_details**

&#x20; **- risk\_other\_indicators\_category\_yes\_no\_unknown**

&#x20; **- risk\_other\_indicators\_details**

&#x20; **- risk\_self\_harm\_category\_yes\_no\_unknown**

&#x20; **- risk\_self\_harm\_details**

&#x20; **- risk\_substance\_misuse\_category\_yes\_no\_unknown**

&#x20; **- risk\_substance\_injected\_category\_yes\_no\_unknown**

&#x20; **- risk\_substance\_misuse\_details**

&#x20; **- risk\_neglect\_vulnerability\_category\_yes\_no\_unknown**

&#x20; **- risk\_neglect\_vulnerability\_details**

&#x20; **- risk\_child\_vulnerable\_adult\_category\_yes\_no\_unknown**

&#x20; **- risk\_child\_vulnerable\_adult\_details**

&#x20; **- risk\_physical\_impairment\_category\_yes\_no\_unknown**

&#x20; **- risk\_physical\_impairment\_details**

&#x20; **- risk\_dissocial\_offending\_category\_yes\_no\_unknown**

&#x20; **- risk\_dissocial\_offending\_details**

&#x20; **- risk\_collateral\_history\_summary**

&#x20; **- summary\_of\_active\_risk**

&#x20; **- summary\_of\_protective\_factors**

&#x20; **- epic\_risk\_assessment\_text**

&#x20; **- summary\_pertinent\_risk\_issues**



**3. Mental State Examination (MSE)**



**(Source: MSE paper forms, Epic EHR, Assessment Summary)**



&#x20; **- mse\_appearance\_behaviour**

&#x20; **- mse\_speech\_thought\_form**

&#x20; **- mse\_mood\_affect**

&#x20; **- mse\_suicidality\_details**

&#x20; **- mse\_suicide\_attempt\_this\_presentation\_yes\_no**

&#x20; **- mse\_access\_weapons\_guns**

&#x20; **- mse\_thought\_content**

&#x20; **- mse\_perceptual\_disturbances**

&#x20; **- mse\_cognition**

&#x20; **- mse\_insight**

&#x20; **- epic\_mse\_appearance\_behaviour**

&#x20; **- epic\_mse\_speech\_thought\_form**

&#x20; **- epic\_mse\_mood\_affect**

&#x20; **- epic\_mse\_suicidality**

&#x20; **- summary\_salient\_features\_mse**



**4. History**



**(Source: Assessment Proforma internal pages, Epic EHR, Assessment Summary)**



&#x20; **- history\_of\_presenting\_complaint\_initial**

&#x20; **- history\_of\_presenting\_complaint\_continued**

&#x20; **- mental\_health\_history\_general**

&#x20; **- mental\_health\_history\_recent\_suicide\_events\_last\_2\_months**

&#x20; **- mental\_health\_history\_recent\_suicide\_events\_before\_2\_months**

&#x20; **- mental\_health\_personal\_history**

&#x20; **- mental\_health\_family\_history**

&#x20; **- mental\_health\_offending\_history**

&#x20; **- family\_history\_details**

&#x20; **- personal\_history\_childhood**

&#x20; **- personal\_history\_education**

&#x20; **- personal\_history\_employment**

&#x20; **- personal\_history\_relationships**

&#x20; **- premorbid\_personality**

&#x20; **- forensic\_history**

&#x20; **- past\_psychiatric\_history**

&#x20; **- past\_medical\_history**

&#x20; **- collateral\_history\_details**

&#x20; **- summary\_of\_history**

&#x20; **- epic\_contact\_details\_general\_overview**



**5. Medications**



**(Source: Medication section, Functional Assessment checkboxes)**



&#x20; **- medication\_details**

&#x20; **- medication\_task\_removing\_from\_packaging\_score (able/able with**

&#x20;   **difficulty/able with equipment/not able)**

&#x20; **- medication\_task\_reading\_labels\_score**

&#x20; **- medication\_task\_consent\_seek\_info\_score**

&#x20; **- medication\_task\_taking\_right\_dose\_score**

&#x20; **- medication\_task\_swallowing\_score**

&#x20; **- medication\_task\_using\_equipment\_score**

&#x20; **- medication\_task\_storing\_safely\_score**

&#x20; **- medication\_task\_disposing\_safely\_score**

&#x20; **- medication\_task\_ordering\_score**

&#x20; **- medication\_task\_collecting\_score**



**6. Substance Use**



**(Source: Substance Misuse section, LDQ, AUDIT-C)**



&#x20; **- substance\_misuse\_current\_use**

&#x20; **- substance\_misuse\_previous\_use**

&#x20; **- substance\_misuse\_complex\_factors**

&#x20; **- audit\_c\_q1\_frequency (0-4 score)**

&#x20; **- audit\_c\_q2\_units\_typical\_day (0-4 score)**

&#x20; **- audit\_c\_q3\_binge\_frequency (0-4 score)**

&#x20; **- audit\_c\_total\_score**

&#x20; **- ldq\_q1\_thinking\_next (0-3 score)**

&#x20; **- ldq\_q2\_more\_important (0-3 score)**

&#x20; **- ldq\_q3\_too\_strong (0-3 score)**

&#x20; **- ldq\_q4\_plan\_days (0-3 score)**

&#x20; **- ldq\_q5\_increase\_effect (0-3 score)**

&#x20; **- ldq\_q6\_morning\_afternoon\_evening (0-3 score)**

&#x20; **- ldq\_q7\_carry\_on (0-3 score)**

&#x20; **- ldq\_q8\_effect\_more\_important (0-3 score)**

&#x20; **- ldq\_q9\_take\_more\_wearing\_off (0-3 score)**

&#x20; **- ldq\_q10\_difficult\_cope (0-3 score)**

&#x20; **- ldq\_total\_score**



**7. Social Circumstances**



**(Source: Assessment Proforma internal pages)**



&#x20; **- mental\_health\_social\_circumstances**

&#x20; **- mental\_health\_occupational\_needs**

&#x20; **- personal\_history\_social\_circumstances**



**8. Child Protection**



**(Source: Child Protection Details form, Assessment Summary)**



&#x20; **- child\_protection\_patient\_name**

&#x20; **- child\_protection\_marital\_status**

&#x20; **- child\_protection\_partner\_name**

&#x20; **- child\_protection\_partner\_address**

&#x20; **- child\_protection\_children\_contact\_list (Array of objects: name, sex, dob,**

&#x20;   **relationship\_patient, relationship\_partner, other\_parent\_name,**

&#x20;   **contact\_yes\_no)**

&#x20; **- child\_protection\_fcc\_involved\_yes\_no**

&#x20; **- child\_protection\_fcc\_involvement\_details**

&#x20; **- child\_protection\_unocini\_referral\_made\_yes\_no**

&#x20; **- child\_protection\_wish\_family\_support\_referral\_yes\_no**

&#x20; **- summary\_child\_protection\_issues\_identified (Yes/No checkbox)**



**9. Safety Planning**



**(Source: Safety Plan form)**



&#x20; **- safety\_plan\_step1\_warning\_signs (Array of 3 strings)**

&#x20; **- safety\_plan\_step2\_internal\_coping\_strategies (Array of 3 strings)**

&#x20; **- safety\_plan\_step3\_distraction\_people\_places (Array of 2 objects: name,**

&#x20;   **place)**

&#x20; **- safety\_plan\_step4\_supporters (Array of 3 objects: name, phone)**

&#x20; **- safety\_plan\_step5\_professionals (Object: professional\_1, phone\_1, gp,**

&#x20;   **gp\_phone)**

&#x20; **- safety\_plan\_step6\_making\_environment\_safe (Array of 2 strings)**

&#x20; **- safety\_plan\_most\_important\_thing\_worth\_living\_for**

&#x20; **- safety\_plan\_follow\_up\_call\_consent\_yes\_no**

&#x20; **- safety\_plan\_follow\_up\_call\_date**



**10. Administrative and Consent**



**(Source: Assessment Proforma front page, Confidentiality/Collateral forms, Risk**

**Management Plans, Assessment Summary, Epic EHR)**



&#x20; **- referral\_agent**

&#x20; **- assessment\_date**

&#x20; **- time\_referral\_received\_mhlt**

&#x20; **- time\_assessment\_started**

&#x20; **- time\_assessment\_finished**

&#x20; **- assessor\_name**

&#x20; **- place\_of\_assessment**

&#x20; **- other\_professional\_services\_involved (Array of objects: name, service,**

&#x20;   **contact\_number)**

&#x20; **- clinical\_information\_referrer**

&#x20; **- epic\_type\_and\_purpose\_of\_contact**

&#x20; **- epic\_interventions\_provided (Array of strings from bulleted list)**

&#x20; **- epic\_plan\_text**

&#x20; **- paris\_assessment\_completed\_checkbox**

&#x20; **- activity\_completed\_by\_practitioner\_checkbox**

&#x20; **- time\_follow\_up\_completed**

&#x20; **- consideration\_physical\_sensory\_limitations\_checkbox**

&#x20; **- consent\_obtained\_checkbox**

&#x20; **- confidentiality\_discussed\_checkbox**

&#x20; **- zero\_tolerance\_discussed\_checkbox**

&#x20; **- consent\_speak\_caregiver\_checkbox**

&#x20; **- confidentiality\_explained\_yes\_no\_other**

&#x20; **- patient\_has\_capacity\_to\_consent\_yes\_no\_other**

&#x20; **- consent\_individual\_seek\_information\_yes\_no\_other**

&#x20; **- consent\_individual\_share\_information\_yes\_no\_other**

&#x20; **- who\_information\_shared\_with\_text**

&#x20; **- consent\_family\_carer\_share\_information\_yes\_no\_other**

&#x20; **- consent\_contact\_patient\_phone\_other\_yes\_no\_other**

&#x20; **- restrictions\_third\_party\_information\_text**

&#x20; **- collateral\_provided\_by\_type**

&#x20; **- collateral\_name**

&#x20; **- collateral\_relationship**

&#x20; **- collateral\_consent\_obtained\_yes\_no**

&#x20; **- risk\_screen\_setting (outpatient/inpatient/voluntary/detained)**

&#x20; **- info\_source\_key\_worker\_checked\_and\_specify**

&#x20; **- info\_source\_service\_user\_checked\_and\_specify**

&#x20; **- info\_source\_clinical\_notes\_checked\_and\_specify**

&#x20; **- info\_source\_gp\_referral\_checked\_and\_specify**

&#x20; **- info\_source\_gp\_direct\_checked\_and\_specify**

&#x20; **- info\_source\_carer\_relative\_checked\_and\_specify**

&#x20; **- info\_source\_police\_probation\_checked\_and\_specify**

&#x20; **- info\_source\_other\_checked\_and\_specify**

&#x20; **- immediate\_risk\_management\_plan (Array of objects: action,**

&#x20;   **person\_responsible, signed)**

&#x20; **- contingency\_arrangements**

&#x20; **- action\_discuss\_mdt\_checkbox**

&#x20; **- action\_specialised\_risk\_assessment\_checkbox**

&#x20; **- action\_keep\_under\_review\_checkbox**

&#x20; **- action\_comprehensive\_risk\_assessment\_checkbox**

&#x20; **- action\_no\_further\_action\_required\_checkbox**

&#x20; **- distribution\_service\_user\_checkbox**

&#x20; **- distribution\_key\_worker\_checkbox**

&#x20; **- distribution\_other\_checkbox\_and\_specify**

&#x20; **- risk\_record\_of\_reviews (Array of objects: date\_time, update\_change\_in\_risk,**

&#x20;   **alteration\_to\_plan, lead\_responsibility, signed)**

&#x20; **- summary\_asp1\_form\_completed\_yes\_no**

&#x20; **- summary\_management\_plan**

&#x20; **- summary\_safety\_leaflet\_given\_yes\_no**

&#x20; **- summary\_outcome\_mdt\_discussion**

&#x20; **- summary\_icd10\_diagnosis\_1**

&#x20; **- summary\_icd10\_diagnosis\_2**

&#x20; **- summary\_icd10\_diagnosis\_3**

&#x20; **- follow\_up\_telephone\_calls\_log (Array of objects: date, contacts)**











